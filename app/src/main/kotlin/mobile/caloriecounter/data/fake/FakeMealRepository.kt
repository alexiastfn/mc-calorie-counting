package mobile.caloriecounter.data.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import mobile.caloriecounter.domain.DaySummary
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.MealEntry
import mobile.caloriecounter.domain.MealRepository
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.domain.MealWithFood
import mobile.caloriecounter.domain.NutritionCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory [MealRepository]. Everything here is lost when the process dies; that is exactly
 * the gap Room closes in M2 (the UI then observes Room instead of this StateFlow).
 */
class FakeMealRepository(
    private val foods: List<Food>,
    initialEntries: List<MealEntry> = emptyList(),
    private val now: () -> LocalDateTime = LocalDateTime::now,
) : MealRepository {

    private val entries = MutableStateFlow(initialEntries)
    private val nextId = AtomicLong((initialEntries.maxOfOrNull { it.id } ?: 0) + 1)

    override fun observeDay(date: LocalDate): Flow<List<MealWithFood>> = entries.map { list ->
        list.filter { it.loggedAt.toLocalDate() == date }
            .sortedBy { it.loggedAt }
            .mapNotNull(::withFood)
    }

    override fun observeEntry(id: Long): Flow<MealWithFood?> = entries.map { list ->
        list.find { it.id == id }?.let(::withFood)
    }

    override fun observeHistory(): Flow<List<DaySummary>> = entries.map { list ->
        list.mapNotNull(::withFood)
            .groupBy { it.entry.loggedAt.toLocalDate() }
            .map { (date, meals) -> DaySummary(date, NutritionCalculator.totalKcal(meals), meals.size) }
            .sortedByDescending { it.date }
    }

    override suspend fun log(foodId: String, grams: Double, mealType: MealType): Long {
        val id = nextId.getAndIncrement()
        entries.update { it + MealEntry(id, foodId, grams, mealType, now()) }
        return id
    }

    override suspend fun update(entry: MealEntry) {
        entries.update { list -> list.map { if (it.id == entry.id) entry else it } }
    }

    override suspend fun delete(id: Long) {
        entries.update { list -> list.filterNot { it.id == id } }
    }

    private fun withFood(entry: MealEntry): MealWithFood? =
        foods.find { it.id == entry.foodId }?.let { MealWithFood(entry, it) }
}
