package mobile.caloriecounter.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mobile.caloriecounter.di.appContainer
import mobile.caloriecounter.domain.SettingsRepository

data class SettingsUiState(
    val goalText: String = "",
    val savedGoalKcal: Int = 0,
    val canSaveGoal: Boolean = false,
    val allowCloudRecognition: Boolean = false,
)

/** Daily goal and the cloud-recognition opt-in. The goal being typed lives in [SavedStateHandle]. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editedGoal = savedStateHandle.getStateFlow<String?>(KEY_GOAL, null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeDailyGoalKcal(),
        settingsRepository.observeAllowCloudRecognition(),
        editedGoal,
    ) { goal, allowCloud, edited ->
        val text = edited ?: goal.toString()
        val parsed = text.trim().toIntOrNull()
        SettingsUiState(
            goalText = text,
            savedGoalKcal = goal,
            canSaveGoal = parsed != null && parsed in 500..10_000 && parsed != goal,
            allowCloudRecognition = allowCloud,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onGoalChange(text: String) {
        savedStateHandle[KEY_GOAL] = text
    }

    fun onSaveGoal() {
        val kcal = uiState.value.goalText.trim().toIntOrNull() ?: return
        viewModelScope.launch {
            settingsRepository.setDailyGoalKcal(kcal)
            savedStateHandle[KEY_GOAL] = null   // back to "showing the stored value"
        }
    }

    fun onAllowCloudChange(allowed: Boolean) {
        viewModelScope.launch { settingsRepository.setAllowCloudRecognition(allowed) }
    }

    companion object {
        private const val KEY_GOAL = "edited_goal"

        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(appContainer().settingsRepository, createSavedStateHandle())
            }
        }
    }
}
