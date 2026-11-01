package mobile.caloriecounter.domain

/** Nutrients for one portion. Each value is null when the food does not have that nutrient. */
data class Nutrients(
    val kcal: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
)

/**
 * Pure calorie math, no Android types, so it is unit-tested on the JVM.
 * Calories are always derived (never stored), so a fix to a food's data updates every past entry.
 */
object NutritionCalculator {

    /** grams * per100g / 100, or null if the value is unknown or grams is negative. */
    fun forGrams(grams: Double, per100g: Double?): Double? {
        if (per100g == null || grams < 0) return null
        return grams * per100g / 100.0
    }

    fun portion(food: Food, grams: Double): Nutrients = Nutrients(
        kcal = forGrams(grams, food.kcalPer100g),
        protein = forGrams(grams, food.proteinPer100g),
        carbs = forGrams(grams, food.carbsPer100g),
        fat = forGrams(grams, food.fatPer100g),
    )

    /** Daily total. Entries whose food has no kcal value count as 0 instead of making the total null. */
    fun totalKcal(meals: List<MealWithFood>): Double =
        meals.sumOf { forGrams(it.entry.grams, it.food.kcalPer100g) ?: 0.0 }
}
