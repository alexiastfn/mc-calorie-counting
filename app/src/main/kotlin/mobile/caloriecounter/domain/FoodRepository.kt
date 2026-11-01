package mobile.caloriecounter.domain

import kotlinx.coroutines.flow.Flow

/** Food data. M1: in-memory fake. M2: Room cache refreshed from Open Food Facts. */
interface FoodRepository {
    /** Foods whose name or brand contains [query] (case-insensitive); empty query returns all. */
    fun search(query: String): Flow<List<Food>>

    fun observeFood(id: String): Flow<Food?>
}
