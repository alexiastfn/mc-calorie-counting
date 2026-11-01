package mobile.caloriecounter.domain

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** The user's meal log. M1: in-memory fake. M2: Room as the source of truth + WorkManager sync. */
interface MealRepository {
    fun observeDay(date: LocalDate): Flow<List<MealWithFood>>

    fun observeEntry(id: Long): Flow<MealWithFood?>

    /** Past days that have entries, newest first. */
    fun observeHistory(): Flow<List<DaySummary>>

    /** Returns the new entry's id. */
    suspend fun log(foodId: String, grams: Double, mealType: MealType): Long

    suspend fun update(entry: MealEntry)

    suspend fun delete(id: Long)
}
