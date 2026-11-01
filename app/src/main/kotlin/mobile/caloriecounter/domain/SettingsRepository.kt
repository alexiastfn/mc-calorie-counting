package mobile.caloriecounter.domain

import kotlinx.coroutines.flow.Flow

/** User settings. M1: in-memory fake. M2: DataStore (goal history in Room's DailyGoal). */
interface SettingsRepository {
    fun observeDailyGoalKcal(): Flow<Int>

    suspend fun setDailyGoalKcal(kcal: Int)

    fun observeAllowCloudRecognition(): Flow<Boolean>

    suspend fun setAllowCloudRecognition(allowed: Boolean)
}
