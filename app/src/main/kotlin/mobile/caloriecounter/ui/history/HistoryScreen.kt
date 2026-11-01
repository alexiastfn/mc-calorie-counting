package mobile.caloriecounter.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mobile.caloriecounter.ui.common.formatKcal
import java.time.LocalDate

@Composable
fun HistoryScreen(
    onDayClick: (LocalDate) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(state, onDayClick)
}

@Composable
fun HistoryContent(state: HistoryUiState, onDayClick: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    // rememberLazyListState is rememberSaveable inside, so the scroll position survives process
    // death. Pure UI state like this does not need the ViewModel (spec, Section 3).
    val listState = rememberLazyListState()
    Column(modifier.padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium)
        if (!state.isLoading && state.days.isEmpty()) Text("No meals logged yet.")
        LazyColumn(state = listState) {
            items(state.days, key = { it.date.toEpochDay() }) { day ->
                Row(
                    Modifier.fillMaxWidth().clickable { onDayClick(day.date) }.padding(vertical = 12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(day.date.toString(), style = MaterialTheme.typography.bodyLarge)
                        val noun = if (day.entryCount == 1) "entry" else "entries"
                        Text("${day.entryCount} $noun", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(formatKcal(day.totalKcal))
                }
            }
        }
    }
}
