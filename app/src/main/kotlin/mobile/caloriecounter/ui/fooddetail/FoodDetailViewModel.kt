package mobile.caloriecounter.ui.fooddetail

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
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.FoodRepository
import mobile.caloriecounter.domain.MealRepository
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.domain.NutritionCalculator
import mobile.caloriecounter.domain.Nutrients
import mobile.caloriecounter.ui.common.parseGrams
import mobile.caloriecounter.ui.navigation.FoodDetailRoute
import java.time.LocalTime

data class FoodDetailUiState(
    val food: Food? = null,
    val gramsText: String = "",
    val mealType: MealType = MealType.SNACK,
    val portion: Nutrients? = null,
    val canLog: Boolean = false,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isLogged: Boolean = false,
)

/**
 * Nutrition for one food and the "Log" action. The grams typed and the meal type live in
 * [SavedStateHandle]: this is the state the process-death demo checks (type 150 g, kill, reopen).
 */
class FoodDetailViewModel(
    private val foodId: String,
    initialMealType: MealType,
    foodRepository: FoodRepository,
    private val mealRepository: MealRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gramsText = savedStateHandle.getStateFlow(KEY_GRAMS, "100")
    private val mealType = savedStateHandle.getStateFlow(KEY_MEAL_TYPE, initialMealType)
    private val isLogged = MutableStateFlow(false)

    val uiState: StateFlow<FoodDetailUiState> = combine(
        foodRepository.observeFood(foodId),
        gramsText,
        mealType,
        isLogged,
    ) { food, text, type, logged ->
        val grams = parseGrams(text)
        FoodDetailUiState(
            food = food,
            gramsText = text,
            mealType = type,
            portion = if (food != null && grams != null) NutritionCalculator.portion(food, grams) else null,
            canLog = food != null && grams != null && !logged,
            isLoading = false,
            notFound = food == null,
            isLogged = logged,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodDetailUiState())

    fun onGramsChange(text: String) {
        savedStateHandle[KEY_GRAMS] = text
    }

    fun onMealTypeChange(type: MealType) {
        savedStateHandle[KEY_MEAL_TYPE] = type
    }

    fun onLog() {
        val state = uiState.value
        val grams = parseGrams(state.gramsText) ?: return
        if (!state.canLog) return
        viewModelScope.launch {
            mealRepository.log(foodId, grams, state.mealType)
            isLogged.value = true
        }
    }

    companion object {
        private const val KEY_GRAMS = "grams"
        private const val KEY_MEAL_TYPE = "selected_meal_type"

        val Factory = viewModelFactory {
            initializer {
                val handle = createSavedStateHandle()
                val route = handle.toRoute<FoodDetailRoute>()
                val container = appContainer()
                FoodDetailViewModel(
                    foodId = route.foodId,
                    initialMealType = route.mealType ?: MealType.forHour(LocalTime.now().hour),
                    foodRepository = container.foodRepository,
                    mealRepository = container.mealRepository,
                    savedStateHandle = handle,
                )
            }
        }
    }
}
