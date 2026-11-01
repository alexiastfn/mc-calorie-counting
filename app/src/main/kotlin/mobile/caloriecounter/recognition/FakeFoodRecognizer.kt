package mobile.caloriecounter.recognition

import mobile.caloriecounter.domain.Engine
import mobile.caloriecounter.domain.FoodRecognizer
import mobile.caloriecounter.domain.LabelScore
import mobile.caloriecounter.domain.RecognitionResult

/**
 * Returns a fixed result. Stands in for OnDeviceFoodRecognizer / CloudFoodRecognizer (M3);
 * [confidence] is configurable so RecognitionPolicy tests can cover both sides of the threshold.
 */
class FakeFoodRecognizer(private val confidence: Float = 0.8f) : FoodRecognizer {
    override suspend fun recognize(imagePath: String): RecognitionResult = RecognitionResult(
        labels = listOf(
            LabelScore("banana", confidence),
            LabelScore("apple", confidence / 2),
            LabelScore("bread", confidence / 4),
        ),
        engine = Engine.ON_DEVICE,
        latencyMs = 0,
    )
}
