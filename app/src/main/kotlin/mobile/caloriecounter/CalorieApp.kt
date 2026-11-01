package mobile.caloriecounter

import android.app.Application
import mobile.caloriecounter.di.AppContainer

/** Owns the app-wide [AppContainer]. In M3 this gets @HiltAndroidApp instead. */
class CalorieApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
