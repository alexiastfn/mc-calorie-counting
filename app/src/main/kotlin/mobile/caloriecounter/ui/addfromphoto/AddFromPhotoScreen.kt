package mobile.caloriecounter.ui.addfromphoto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Placeholder. M3: Photo Picker -> barcode scan -> FoodRecognizer (on-device first, cloud opt-in). */
@Composable
fun AddFromPhotoScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Add from photo", style = MaterialTheme.typography.headlineMedium)
        Text("Coming in M3: pick a photo, scan a barcode or recognize the food, then confirm the grams.")
    }
}
