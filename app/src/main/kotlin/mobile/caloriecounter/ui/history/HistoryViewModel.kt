package mobile.caloriecounter.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mobile.caloriecounter.di.appContainer
import mobile.caloriecounter.domain.DaySummary
import mobile.caloriecounter.domain.MealRepository

data class HistoryUiState(
    val days: List<DaySummary> = emptyList(),
    val isLoading: Boolean = true,
)

class HistoryViewModel(mealRepository: MealRepository) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = mealRepository.observeHistory()
        .map { HistoryUiState(days = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    companion object {
        val Factory = viewModelFactory {
            initializer { HistoryViewModel(appContainer().mealRepository) }
        }
    }
}
