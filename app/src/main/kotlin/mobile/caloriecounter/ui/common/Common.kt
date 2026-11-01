package mobile.caloriecounter.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import mobile.caloriecounter.domain.MealType
import kotlin.math.roundToInt

/**
 * Product image via Coil. Coil loads and decodes off the main thread and caches on disk.
 * placeholder = while loading, error = the URL failed, fallback = there is no URL at all.
 */
@Composable
fun FoodImage(imageUrl: String?, contentDescription: String, size: Dp, modifier: Modifier = Modifier) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
        fallback = ColorPainter(MaterialTheme.colorScheme.secondaryContainer),
        contentScale = ContentScale.Crop,
        modifier = modifier.size(size),
    )
}

/** Stateless meal type picker: shows [selected], reports taps through [onSelect]. */
@Composable
fun MealTypeChips(selected: MealType, onSelect: (MealType) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MealType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(type.label()) },
            )
        }
    }
}

fun MealType.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }

/** "375 kcal", or "? kcal" when the food has no calorie data. */
fun formatKcal(kcal: Double?): String = if (kcal == null) "? kcal" else "${kcal.roundToInt()} kcal"

/** Parses what the user typed into a grams field. Accepts "150" or "150,5"; null if invalid. */
fun parseGrams(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 && it <= 5000 }
