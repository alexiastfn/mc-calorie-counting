package mobile.caloriecounter.ui.navigation

import kotlinx.serialization.Serializable
import mobile.caloriecounter.domain.MealType

/*
 * Type-safe routes. Each one carries only small ids/arguments, never whole objects: the destination
 * looks its data up from the repository. Route arguments are saved with the back stack, so they
 * come back after process death without any extra code.
 */

/** [date] is ISO yyyy-MM-dd; null means today. History opens Today for a past date. */
@Serializable data class TodayRoute(val date: String? = null)

@Serializable data class SearchRoute(val mealType: MealType? = null)

@Serializable data class FoodDetailRoute(val foodId: String, val mealType: MealType? = null)

@Serializable data class MealDetailRoute(val entryId: Long)

@Serializable object AddFromPhotoRoute

@Serializable object HistoryRoute

@Serializable object SettingsRoute
