package mobile.caloriecounter.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mobile.caloriecounter.domain.Food
import mobile.caloriecounter.domain.MealType
import mobile.caloriecounter.ui.common.FoodImage
import mobile.caloriecounter.ui.common.MealTypeChips
import mobile.caloriecounter.ui.common.formatKcal

@Composable
fun SearchScreen(
    onFoodClick: (foodId: String, mealType: MealType) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onMealTypeChange = viewModel::onMealTypeChange,
        onFoodClick = { foodId -> onFoodClick(foodId, state.mealType) },
        onBack = onBack,
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onFoodClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text("Search food or brand") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        MealTypeChips(selected = state.mealType, onSelect = onMealTypeChange)
        if (state.results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No matching foods") }
        } else {
            LazyColumn {
                items(state.results, key = { it.id }) { food ->
                    FoodRow(food, onClick = { onFoodClick(food.id) })
                }
            }
        }
    }
}

@Composable
private fun FoodRow(food: Food, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FoodImage(food.imageUrl, food.name, size = 48.dp)
        Column(Modifier.weight(1f)) {
            Text(food.name, style = MaterialTheme.typography.bodyLarge)
            food.brand?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        Text(if (food.kcalPer100g == null) "incomplete" else "${formatKcal(food.kcalPer100g)}/100 g")
    }
}
