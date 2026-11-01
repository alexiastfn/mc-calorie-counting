package mobile.caloriecounter.ui.today

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mobile.caloriecounter.data.fake.FakeMealRepository
import mobile.caloriecounter.data.fake.FakeSettingsRepository
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.MealType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    private val day = LocalDate.of(2026, 10, 31)
    private val food = Food("f1", "Test food", null, 250.0, null, null, null, null)

    // viewModelScope runs on Dispatchers.Main, which does not exist on the JVM: replace it.
    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `totals update when the fake repository emits, and goal exceeded flips`() = runTest {
        val meals = FakeMealRepository(listOf(food), now = { day.atTime(12, 0) })
        val settings = FakeSettingsRepository(dailyGoalKcal = 500)
        val viewModel = TodayViewModel(day, meals, settings)
        // stateIn(WhileSubscribed) only runs while someone collects, like the screen does.
        // Unconfined = the collector starts right away instead of waiting to be scheduled.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(0.0, viewModel.uiState.value.totalKcal, 1e-9)
        assertEquals(500, viewModel.uiState.value.goalKcal)

        meals.log("f1", grams = 150.0, mealType = MealType.LUNCH)       // 375 kcal
        assertEquals(375.0, viewModel.uiState.value.totalKcal, 1e-9)
        assertFalse(viewModel.uiState.value.goalExceeded)

        meals.log("f1", grams = 100.0, mealType = MealType.DINNER)      // +250 = 625 kcal
        assertEquals(625.0, viewModel.uiState.value.totalKcal, 1e-9)
        assertTrue(viewModel.uiState.value.goalExceeded)
        assertEquals(setOf(MealType.LUNCH, MealType.DINNER), viewModel.uiState.value.mealsByType.keys)
    }
}
