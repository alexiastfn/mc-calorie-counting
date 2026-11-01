package mobile.caloriecounter.ui.mealdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mobile.caloriecounter.di.appContainer
import mobile.caloriecounter.domain.MealEntry
import mobile.caloriecounter.domain.MealRepository
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.domain.NutritionCalculator
import mobile.caloriecounter.ui.common.parseGrams
import mobile.caloriecounter.ui.navigation.MealDetailRoute
import kotlin.math.roundToInt

data class MealDetailUiState(
    val entry: MealEntry? = null,
    val foodName: String = "",
    val imageUrl: String? = null,
    val gramsText: String = "",
    val mealType: MealType = MealType.SNACK,
    val kcal: Double? = null,
    val canSave: Boolean = false,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isDone: Boolean = false,      // saved or deleted: the screen should close
)

/**
 * Edit or delete one logged entry. The edits the user types live in [SavedStateHandle], so they
 * survive process death; null there means "not edited yet, show the stored value".
 */
class MealDetailViewModel(
    entryId: Long,
    private val mealRepository: MealRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editedGrams = savedStateHandle.getStateFlow<String?>(KEY_GRAMS, null)
    private val editedMealType = savedStateHandle.getStateFlow<MealType?>(KEY_MEAL_TYPE, null)
    private val isDone = MutableStateFlow(false)

    val uiState: StateFlow<MealDetailUiState> = combine(
        mealRepository.observeEntry(entryId),
        editedGrams,
        editedMealType,
        isDone,
    ) { meal, grams, mealType, done ->
        when {
            done -> MealDetailUiState(isLoading = false, isDone = true)
            meal == null -> MealDetailUiState(isLoading = false, notFound = true)
            else -> {
                val gramsText = grams ?: meal.entry.grams.roundToInt().toString()
                val parsed = parseGrams(gramsText)
                MealDetailUiState(
                    entry = meal.entry,
                    foodName = meal.food.name,
                    imageUrl = meal.food.imageUrl,
                    gramsText = gramsText,
                    mealType = mealType ?: meal.entry.mealType,
                    kcal = parsed?.let { NutritionCalculator.forGrams(it, meal.food.kcalPer100g) },
                    canSave = parsed != null,
                    isLoading = false,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealDetailUiState())

    fun onGramsChange(text: String) {
        savedStateHandle[KEY_GRAMS] = text
    }

    fun onMealTypeChange(type: MealType) {
        savedStateHandle[KEY_MEAL_TYPE] = type
    }

    fun onSave() {
        val state = uiState.value
        val entry = state.entry ?: return
        val grams = parseGrams(state.gramsText) ?: return
        viewModelScope.launch {
            mealRepository.update(entry.copy(grams = grams, mealType = state.mealType))
            isDone.value = true
        }
    }

    fun onDelete() {
        val entry = uiState.value.entry ?: return
        viewModelScope.launch {
            mealRepository.delete(entry.id)
            isDone.value = true
        }
    }

    companion object {
        private const val KEY_GRAMS = "edited_grams"
        private const val KEY_MEAL_TYPE = "edited_meal_type"

        val Factory = viewModelFactory {
            initializer {
                val handle = createSavedStateHandle()
                MealDetailViewModel(
                    entryId = handle.toRoute<MealDetailRoute>().entryId,
                    mealRepository = appContainer().mealRepository,
                    savedStateHandle = handle,
                )
            }
        }
    }
}
