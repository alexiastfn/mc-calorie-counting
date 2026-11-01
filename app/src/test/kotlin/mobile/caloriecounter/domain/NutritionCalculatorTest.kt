package mobile.caloriecounter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class NutritionCalculatorTest {

    private val food = Food(
        id = "f1", name = "Test food", brand = null,
        kcalPer100g = 250.0, proteinPer100g = 10.0, carbsPer100g = 30.0, fatPer100g = null,
        imageUrl = null,
    )

    @Test
    fun `150 g at 250 kcal per 100 g is 375 kcal`() {
        assertEquals(375.0, NutritionCalculator.forGrams(150.0, 250.0)!!, 1e-9)
    }

    @Test
    fun `unknown per-100g value gives null, not a crash`() {
        assertNull(NutritionCalculator.forGrams(150.0, null))
    }

    @Test
    fun `negative grams gives null`() {
        assertNull(NutritionCalculator.forGrams(-1.0, 250.0))
    }

    @Test
    fun `portion scales every known nutrient and keeps missing ones null`() {
        val n = NutritionCalculator.portion(food, 200.0)
        assertEquals(500.0, n.kcal!!, 1e-9)
        assertEquals(20.0, n.protein!!, 1e-9)
        assertEquals(60.0, n.carbs!!, 1e-9)
        assertNull(n.fat)
    }

    @Test
    fun `daily total sums entries and counts foods without kcal as zero`() {
        val noKcal = food.copy(id = "f2", kcalPer100g = null)
        val meals = listOf(
            MealWithFood(entry(1, grams = 100.0), food),   // 250
            MealWithFood(entry(2, grams = 50.0), food),    // 125
            MealWithFood(entry(3, grams = 80.0), noKcal),  // 0
        )
        assertEquals(375.0, NutritionCalculator.totalKcal(meals), 1e-9)
    }

    private fun entry(id: Long, grams: Double) = MealEntry(
        id = id, foodId = "f1", grams = grams, mealType = MealType.LUNCH,
        loggedAt = LocalDateTime.of(2026, 10, 31, 12, 0),
    )
}
