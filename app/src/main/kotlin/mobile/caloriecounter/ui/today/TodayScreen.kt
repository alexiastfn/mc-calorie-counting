package mobile.caloriecounter.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.ui.common.formatKcal
import mobile.caloriecounter.ui.common.label
import java.time.LocalDate
import kotlin.math.roundToInt

/** Stateful entry point: gets the ViewModel and hands plain state + callbacks to [TodayContent]. */
@Composable
fun TodayScreen(
    onMealClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onPhotoClick: () -> Unit,
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayContent(state, onMealClick, onSearchClick, onPhotoClick)
}

@Composable
fun TodayContent(
    state: TodayUiState,
    onMealClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 160.dp),   // room for the FABs
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                val title = if (state.date == LocalDate.now()) "Today" else state.date.toString()
                Text(title, style = MaterialTheme.typography.headlineMedium)
            }
            item { TotalsCard(state.totalKcal, state.goalKcal, state.goalExceeded) }
            if (!state.isLoading && state.mealsByType.isEmpty()) {
                item { Text("No meals logged yet. Tap \"Add food\" to log one.") }
            }
            MealType.entries.forEach { type ->
                val rows = state.mealsByType[type].orEmpty()
                if (rows.isNotEmpty()) {
                    item(key = type.name) {
                        Text(type.label(), style = MaterialTheme.typography.titleMedium)
                    }
                    items(rows, key = { it.entryId }) { row ->
                        MealRowItem(row, onClick = { onMealClick(row.entryId) })
                    }
                }
            }
        }
        // The spec's "FAB menu": a primary action (search) plus a secondary one (photo).
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SmallFloatingActionButton(onClick = onPhotoClick) { Text("Photo", Modifier.padding(8.dp)) }
            ExtendedFloatingActionButton(onClick = onSearchClick) { Text("Add food") }
        }
    }
}

@Composable
private fun TotalsCard(totalKcal: Double, goalKcal: Int, goalExceeded: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${totalKcal.roundToInt()} / $goalKcal kcal",
                style = MaterialTheme.typography.titleLarge,
                color = if (goalExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            LinearProgressIndicator(
                progress = { if (goalKcal > 0) (totalKcal / goalKcal).toFloat().coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxWidth(),
            )
            if (goalExceeded) Text("Daily goal exceeded", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MealRowItem(row: MealRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.foodName, style = MaterialTheme.typography.bodyLarge)
            Text("${row.grams.roundToInt()} g", style = MaterialTheme.typography.bodySmall)
        }
        Text(formatKcal(row.kcal))
    }
}
