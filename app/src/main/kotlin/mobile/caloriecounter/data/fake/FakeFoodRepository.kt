package mobile.caloriecounter.data.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.FoodRepository

/** In-memory [FoodRepository]. Replaced by a Room + Open Food Facts implementation in M2. */
class FakeFoodRepository(foods: List<Food>) : FoodRepository {

    private val foods = MutableStateFlow(foods)

    override fun search(query: String): Flow<List<Food>> = foods.map { all ->
        val q = query.trim()
        if (q.isEmpty()) all
        else all.filter {
            it.name.contains(q, ignoreCase = true) || it.brand?.contains(q, ignoreCase = true) == true
        }
    }

    override fun observeFood(id: String): Flow<Food?> = foods.map { all -> all.find { it.id == id } }
}
