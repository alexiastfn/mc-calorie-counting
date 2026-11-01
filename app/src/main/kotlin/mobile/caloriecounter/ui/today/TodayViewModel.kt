package mobile.caloriecounter.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import mobile.caloriecounter.di.appContainer
import mobile.caloriecounter.domain.MealRepository
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.domain.NutritionCalculator
import mobile.caloriecounter.domain.SettingsRepository
import mobile.caloriecounter.ui.navigation.TodayRoute
import java.time.LocalDate

/** One row in the Today list. */
data class MealRow(
    val entryId: Long,
    val foodName: String,
    val grams: Double,
    val kcal: Double?,
)

data class TodayUiState(
    val date: LocalDate,
    val mealsByType: Map<MealType, List<MealRow>> = emptyMap(),
    val totalKcal: Double = 0.0,
    val goalKcal: Int = 0,
    val isLoading: Boolean = true,
) {
    val goalExceeded: Boolean get() = goalKcal > 0 && totalKcal > goalKcal
}

/**
 * Today's meals and totals vs. goal. Read-only: it only combines two repository flows.
 * [date] comes from the route (a plain argument here, so the JVM test can pass it directly).
 */
class TodayViewModel(
    date: LocalDate,
    mealRepository: MealRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = combine(
        mealRepository.observeDay(date),
        settingsRepository.observeDailyGoalKcal(),
    ) { meals, goal ->
        TodayUiState(
            date = date,
            mealsByType = meals
                .groupBy { it.entry.mealType }
                .mapValues { (_, list) ->
                    list.map {
                        MealRow(
                            entryId = it.entry.id,
                            foodName = it.food.name,
                            grams = it.entry.grams,
                            kcal = NutritionCalculator.forGrams(it.entry.grams, it.food.kcalPer100g),
                        )
                    }
                },
            totalKcal = NutritionCalculator.totalKcal(meals),
            goalKcal = goal,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        // Keep the upstream alive 5 s after the UI stops collecting, so a rotation does not restart it.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(date = date),
    )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val route = createSavedStateHandle().toRoute<TodayRoute>()
                val container = appContainer()
                TodayViewModel(
                    date = route.date?.let(LocalDate::parse) ?: LocalDate.now(),
                    mealRepository = container.mealRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
