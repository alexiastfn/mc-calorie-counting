package mobile.caloriecounter.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import mobile.caloriecounter.CalorieApp
import mobile.caloriecounter.data.fake.FakeFoodRepository
import mobile.caloriecounter.data.fake.FakeMealRepository
import mobile.caloriecounter.data.fake.FakeSettingsRepository
import mobile.caloriecounter.data.fake.SampleData
import mobile.caloriecounter.domain.FoodRecognizer
import mobile.caloriecounter.domain.FoodRepository
import mobile.caloriecounter.domain.MealRepository
import mobile.caloriecounter.domain.SettingsRepository
import mobile.caloriecounter.recognition.FakeFoodRecognizer
import java.time.LocalDate

/**
 * Manual dependency injection: one instance per app process, created in [CalorieApp.onCreate].
 * Everything is typed by its domain interface, so swapping a fake for Room/Retrofit only changes
 * this file. In M3 this class becomes Hilt @Module(s) installed in SingletonComponent.
 */
class AppContainer {
    private val today = LocalDate.now()

    val foodRepository: FoodRepository = FakeFoodRepository(SampleData.foods)
    val mealRepository: MealRepository = FakeMealRepository(SampleData.foods, SampleData.meals(today))
    val settingsRepository: SettingsRepository = FakeSettingsRepository()
    val foodRecognizer: FoodRecognizer = FakeFoodRecognizer()
}

/** Lets a ViewModel factory's `initializer { }` block reach the container: `appContainer()`. */
fun CreationExtras.appContainer(): AppContainer = (this[APPLICATION_KEY] as CalorieApp).container
