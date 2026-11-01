package mobile.caloriecounter.data.fake

import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.MealEntry
import mobile.caloriecounter.domain.MealType
import java.time.LocalDate

/**
 * Seed data for the M1 fakes. The packaged products are real Open Food Facts entries (fetched
 * 2026-10-31); Nesquik really has no nutriments there, which exercises the "incomplete" path.
 */
object SampleData {

    val foods: List<Food> = listOf(
        Food(
            "3017620422003", "Nutella", "Ferrero",
            kcalPer100g = 539.0, proteinPer100g = 6.3, carbsPer100g = 57.5, fatPer100g = 30.9,
            imageUrl = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front_en.879.200.jpg",
        ),
        Food(
            "5449000000996", "Coca-Cola Original", "Coca-Cola",
            kcalPer100g = 42.4, proteinPer100g = 0.0, carbsPer100g = 10.6, fatPer100g = 0.0,
            imageUrl = "https://images.openfoodfacts.org/images/products/544/900/000/0996/front_en.1129.200.jpg",
        ),
        Food(
            "3175680011480", "Sésame crackers", "Gerblé",
            kcalPer100g = 467.0, proteinPer100g = 10.0, carbsPer100g = 64.0, fatPer100g = 18.0,
            imageUrl = "https://images.openfoodfacts.org/images/products/317/568/001/1480/front_fr.325.200.jpg",
        ),
        Food(
            "8000500310427", "Nutella Biscuits", "Ferrero",
            kcalPer100g = 515.0, proteinPer100g = 7.9, carbsPer100g = 64.0, fatPer100g = 24.8,
            imageUrl = "https://images.openfoodfacts.org/images/products/800/050/031/0427/front_fr.590.200.jpg",
        ),
        Food(
            "3033710065967", "Nesquik Cacao", "Nestlé",
            kcalPer100g = null, proteinPer100g = null, carbsPer100g = null, fatPer100g = null,
            imageUrl = "https://images.openfoodfacts.org/images/products/303/371/006/5967/front_en.472.200.jpg",
        ),
        // Manual foods: no image (placeholder path) and a broken URL (error path).
        Food(
            "manual-banana", "Banana", null,
            kcalPer100g = 89.0, proteinPer100g = 1.1, carbsPer100g = 22.8, fatPer100g = 0.3,
            imageUrl = null,
        ),
        Food(
            "manual-oats", "Rolled oats", null,
            kcalPer100g = 379.0, proteinPer100g = 13.2, carbsPer100g = 67.7, fatPer100g = 6.5,
            imageUrl = "https://images.openfoodfacts.org/does-not-exist.jpg",
        ),
    )

    /** A few entries for [today] and the two days before, so Today and History are not empty. */
    fun meals(today: LocalDate): List<MealEntry> = listOf(
        MealEntry(1, "manual-oats", 60.0, MealType.BREAKFAST, today.atTime(8, 0)),
        MealEntry(2, "manual-banana", 120.0, MealType.BREAKFAST, today.atTime(8, 5)),
        MealEntry(3, "3175680011480", 40.0, MealType.SNACK, today.atTime(11, 0)),
        MealEntry(4, "3017620422003", 30.0, MealType.BREAKFAST, today.minusDays(1).atTime(9, 0)),
        MealEntry(5, "5449000000996", 330.0, MealType.LUNCH, today.minusDays(1).atTime(13, 0)),
        MealEntry(6, "8000500310427", 50.0, MealType.SNACK, today.minusDays(2).atTime(16, 0)),
    )
}
