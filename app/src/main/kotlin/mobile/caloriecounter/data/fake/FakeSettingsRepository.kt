package mobile.caloriecounter.data.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import mobile.caloriecounter.domain.SettingsRepository

/** In-memory [SettingsRepository]. Replaced by DataStore in M2. */
class FakeSettingsRepository(
    dailyGoalKcal: Int = 2000,
    allowCloudRecognition: Boolean = false,   // spec: cloud recognition is opt-in
) : SettingsRepository {

    private val goal = MutableStateFlow(dailyGoalKcal)
    private val allowCloud = MutableStateFlow(allowCloudRecognition)

    override fun observeDailyGoalKcal(): Flow<Int> = goal

    override suspend fun setDailyGoalKcal(kcal: Int) {
        goal.value = kcal
    }

    override fun observeAllowCloudRecognition(): Flow<Boolean> = allowCloud

    override suspend fun setAllowCloudRecognition(allowed: Boolean) {
        allowCloud.value = allowed
    }
}
