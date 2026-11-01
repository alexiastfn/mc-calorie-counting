package mobile.caloriecounter.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mobile.caloriecounter.ui.addfromphoto.AddFromPhotoScreen
import mobile.caloriecounter.ui.fooddetail.FoodDetailScreen
import mobile.caloriecounter.ui.history.HistoryScreen
import mobile.caloriecounter.ui.mealdetail.MealDetailScreen
import mobile.caloriecounter.ui.search.SearchScreen
import mobile.caloriecounter.ui.settings.SettingsScreen
import mobile.caloriecounter.ui.today.TodayScreen
import kotlin.reflect.KClass

/** The three top-level destinations shown in the bottom bar. */
private enum class TopLevel(val label: String, val route: Any, val routeClass: KClass<*>) {
    TODAY("Today", TodayRoute(), TodayRoute::class),
    HISTORY("History", HistoryRoute, HistoryRoute::class),
    SETTINGS("Settings", SettingsRoute, SettingsRoute::class),
}

/**
 * The whole navigation graph. Screens never see the NavController: they get plain callbacks,
 * and this file decides what each callback navigates to.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    Scaffold(bottomBar = { BottomBar(navController) }) { padding ->
        NavHost(
            navController = navController,
            startDestination = TodayRoute(),
            modifier = Modifier.padding(padding),
        ) {
            composable<TodayRoute> {
                TodayScreen(
                    onMealClick = { entryId -> navController.navigate(MealDetailRoute(entryId)) },
                    onSearchClick = { navController.navigate(SearchRoute()) },
                    onPhotoClick = { navController.navigate(AddFromPhotoRoute) },
                )
            }
            composable<SearchRoute> {
                SearchScreen(
                    onFoodClick = { foodId, mealType ->
                        navController.navigate(FoodDetailRoute(foodId, mealType))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<FoodDetailRoute> {
                FoodDetailScreen(
                    // After logging, return to the Today screen that started the flow.
                    onLogged = { navController.popBackStack<TodayRoute>(inclusive = false) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<MealDetailRoute> {
                MealDetailScreen(onDone = { navController.popBackStack() })
            }
            composable<AddFromPhotoRoute> {
                AddFromPhotoScreen(onBack = { navController.popBackStack() })
            }
            composable<HistoryRoute> {
                HistoryScreen(onDayClick = { date -> navController.navigate(TodayRoute(date.toString())) })
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    NavigationBar {
        TopLevel.entries.forEach { item ->
            NavigationBarItem(
                selected = destination?.hierarchy?.any { it.hasRoute(item.routeClass) } == true,
                onClick = {
                    // Standard top-level navigation: one copy of each tab, and each tab keeps its
                    // own back stack when you switch away and come back.
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(item.label.take(1)) },   // no icon library in M1; a letter is enough
                label = { Text(item.label) },
            )
        }
    }
}
