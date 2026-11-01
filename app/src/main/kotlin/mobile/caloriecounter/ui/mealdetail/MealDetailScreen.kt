package mobile.caloriecounter.ui.mealdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.ui.common.FoodImage
import mobile.caloriecounter.ui.common.MealTypeChips
import mobile.caloriecounter.ui.common.formatKcal

@Composable
fun MealDetailScreen(
    onDone: () -> Unit,
    viewModel: MealDetailViewModel = viewModel(factory = MealDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Navigation is a side effect of state: once saved/deleted, leave the screen.
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    MealDetailContent(
        state = state,
        onGramsChange = viewModel::onGramsChange,
        onMealTypeChange = viewModel::onMealTypeChange,
        onSave = viewModel::onSave,
        onDelete = viewModel::onDelete,
        onBack = onDone,
    )
}

@Composable
fun MealDetailContent(
    state: MealDetailUiState,
    onGramsChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        when {
            state.isLoading -> Text("Loading…")
            // In M1 the fakes are in memory, so an entry logged before process death is gone.
            state.notFound -> Text("This meal no longer exists.")
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FoodImage(state.imageUrl, state.foodName, size = 72.dp)
                    Text(state.foodName, style = MaterialTheme.typography.headlineSmall)
                }
                OutlinedTextField(
                    value = state.gramsText,
                    onValueChange = onGramsChange,
                    label = { Text("Grams") },
                    isError = !state.canSave,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MealTypeChips(selected = state.mealType, onSelect = onMealTypeChange)
                Text(formatKcal(state.kcal), style = MaterialTheme.typography.titleLarge)
                Text("Sync: local only (backup arrives in M2)", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onSave, enabled = state.canSave) { Text("Save") }
                    OutlinedButton(onClick = onDelete) { Text("Delete") }
                }
            }
        }
    }
}
