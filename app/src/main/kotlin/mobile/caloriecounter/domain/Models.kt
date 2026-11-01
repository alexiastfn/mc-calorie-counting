package mobile.caloriecounter.domain

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A food with nutrition per 100 g. Nutrients are nullable because Open Food Facts data is
 * volunteer-contributed and often incomplete; a null shows as "incomplete", never as a crash.
 */
data class Food(
    val id: String,              // barcode / OFF code, or a local id for manual foods
    val name: String,
    val brand: String?,
    val kcalPer100g: Double?,
    val proteinPer100g: Double?,
    val carbsPer100g: Double?,
    val fatPer100g: Double?,
    val imageUrl: String?,
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK;

    companion object {
        /** A sensible pre-selection when the user did not pick a meal type. */
        fun forHour(hour: Int): MealType = when (hour) {
            in 5..10 -> BREAKFAST
            in 11..15 -> LUNCH
            in 18..22 -> DINNER
            else -> SNACK
        }
    }
}

/** One logged portion. Calories are not stored here: they are derived by [NutritionCalculator]. */
data class MealEntry(
    val id: Long,
    val foodId: String,
    val grams: Double,
    val mealType: MealType,
    val loggedAt: LocalDateTime,
)

/** An entry joined with its food, which is what the Today and MealDetail screens show. */
data class MealWithFood(
    val entry: MealEntry,
    val food: Food,
)

/** One row of the History screen. */
data class DaySummary(
    val date: LocalDate,
    val totalKcal: Double,
    val entryCount: Int,
)
