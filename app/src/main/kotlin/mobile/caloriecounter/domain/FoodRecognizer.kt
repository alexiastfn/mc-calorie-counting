package mobile.caloriecounter.domain

/**
 * Recognizes food in a photo. M3 adds two implementations (on-device ML Kit / LiteRT, cloud Gemini)
 * behind this one interface, so the placement decision does not leak into ViewModels.
 */
interface FoodRecognizer {
    suspend fun recognize(imagePath: String): RecognitionResult
}

data class RecognitionResult(
    val labels: List<LabelScore>,
    val engine: Engine,
    val latencyMs: Long,
)

data class LabelScore(
    val name: String,
    val confidence: Float,       // 0..1
)

enum class Engine { ON_DEVICE, CLOUD }
