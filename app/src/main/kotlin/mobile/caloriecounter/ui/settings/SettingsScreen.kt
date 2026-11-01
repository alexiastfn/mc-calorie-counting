package mobile.caloriecounter.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        onGoalChange = viewModel::onGoalChange,
        onSaveGoal = viewModel::onSaveGoal,
        onAllowCloudChange = viewModel::onAllowCloudChange,
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    onGoalChange: (String) -> Unit,
    onSaveGoal: () -> Unit,
    onAllowCloudChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.goalText,
                onValueChange = onGoalChange,
                label = { Text("Daily goal (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onSaveGoal, enabled = state.canSaveGoal) { Text("Save") }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Allow cloud recognition", modifier = Modifier.weight(1f))
            Switch(checked = state.allowCloudRecognition, onCheckedChange = onAllowCloudChange)
        }
        Text("Notifications: not used yet (permission flow arrives in M2)", style = MaterialTheme.typography.bodySmall)
        HorizontalDivider()
        Text(
            "Food data and images: Open Food Facts (openfoodfacts.org), Open Database License.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
