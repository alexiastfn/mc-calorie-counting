package mobile.caloriecounter.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import mobile.caloriecounter.di.appContainer
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.FoodRepository
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.ui.navigation.SearchRoute
import java.time.LocalTime

data class SearchUiState(
    val query: String = "",
    val mealType: MealType = MealType.SNACK,
    val results: List<Food> = emptyList(),
)

/** Food search. Query text and meal type live in [SavedStateHandle] so they survive process death. */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    initialMealType: MealType,
    foodRepository: FoodRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query = savedStateHandle.getStateFlow(KEY_QUERY, "")
    private val mealType = savedStateHandle.getStateFlow(KEY_MEAL_TYPE, initialMealType)

    // flatMapLatest: a new query cancels the previous search. M2 adds debounce(400 ms) here,
    // once each query can hit Open Food Facts (10 req/min limit).
    private val results = query.flatMapLatest { foodRepository.search(it) }

    val uiState: StateFlow<SearchUiState> = combine(query, mealType, results) { q, type, foods ->
        SearchUiState(q, type, foods)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState(mealType = initialMealType))

    fun onQueryChange(text: String) {
        savedStateHandle[KEY_QUERY] = text
    }

    fun onMealTypeChange(type: MealType) {
        savedStateHandle[KEY_MEAL_TYPE] = type
    }

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_MEAL_TYPE = "selected_meal_type"

        val Factory = viewModelFactory {
            initializer {
                val handle = createSavedStateHandle()
                SearchViewModel(
                    initialMealType = handle.toRoute<SearchRoute>().mealType
                        ?: MealType.forHour(LocalTime.now().hour),
                    foodRepository = appContainer().foodRepository,
                    savedStateHandle = handle,
                )
            }
        }
    }
}
