package mobile.caloriecounter.ui.fooddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.domain.Nutrients
import mobile.caloriecounter.ui.common.FoodImage
import mobile.caloriecounter.ui.common.MealTypeChips
import mobile.caloriecounter.ui.common.formatKcal
import kotlin.math.roundToInt

@Composable
fun FoodDetailScreen(
    onLogged: () -> Unit,
    onBack: () -> Unit,
    viewModel: FoodDetailViewModel = viewModel(factory = FoodDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLogged) { if (state.isLogged) onLogged() }
    FoodDetailContent(
        state = state,
        onGramsChange = viewModel::onGramsChange,
        onMealTypeChange = viewModel::onMealTypeChange,
        onLog = viewModel::onLog,
        onBack = onBack,
    )
}

@Composable
fun FoodDetailContent(
    state: FoodDetailUiState,
    onGramsChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onLog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        val food = state.food
        when {
            state.isLoading -> Text("Loading…")
            state.notFound || food == null -> Text("Food not found.")
            else -> {
                FoodImage(food.imageUrl, food.name, size = 160.dp)
                Text(food.name, style = MaterialTheme.typography.headlineSmall)
                food.brand?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Per100g(food)
                HorizontalDivider()
                OutlinedTextField(
                    value = state.gramsText,
                    onValueChange = onGramsChange,
                    label = { Text("Grams") },
                    isError = state.portion == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MealTypeChips(selected = state.mealType, onSelect = onMealTypeChange)
                state.portion?.let { PortionSummary(it) }
                Button(onClick = onLog, enabled = state.canLog, modifier = Modifier.fillMaxWidth()) {
                    Text("Log")
                }
            }
        }
    }
}

@Composable
private fun Per100g(food: Food) {
    if (food.kcalPer100g == null) {
        Text("Nutrition data incomplete for this product.", color = MaterialTheme.colorScheme.error)
    }
    Text(
        "Per 100 g: ${formatKcal(food.kcalPer100g)} · P ${g(food.proteinPer100g)} · " +
            "C ${g(food.carbsPer100g)} · F ${g(food.fatPer100g)}",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun PortionSummary(portion: Nutrients) {
    Text(formatKcal(portion.kcal), style = MaterialTheme.typography.titleLarge)
    Text("P ${g(portion.protein)} · C ${g(portion.carbs)} · F ${g(portion.fat)}")
}

private fun g(value: Double?): String = if (value == null) "?" else "${value.roundToInt()} g"
