package pro.devapp.walkietalkiek.feature.ptt

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
// For Dp units, if needed, though current implementation uses Float for pixels
// import androidx.compose.ui.unit.dp

import kotlinx.coroutines.delay

// Default values based on the original View or sensible defaults
private const val DEFAULT_ITEMS_COUNT = 80
private const val ANIMATION_DELAY_MS = 300L
// This normalization factor is crucial. The original View used 20f.
// It means an average amplitude of 20 (from byte values -128 to 127)
// would scale to the full height of the canvas component.
private const val DEFAULT_AMPLITUDE_NORMALIZATION: Float = 20.0f
// Default color, similar to a typical R.color.colorAccent from Android themes
private val DEFAULT_WAVE_COLOR = Color(0xFF_FF4081) // Material Pink A200 is a common accent
private const val DEFAULT_STROKE_WIDTH_PX: Float = 20.0f // Original View used 20f

/**
 * A Jetpack Compose function that mimics the behavior of the provided Android WaveView.
 * It visualizes audio data as a series of vertical lines.
 *
 * @param modifier Modifier for this composable.
 * @param latestAudioData A Pair containing the latest ByteArray of audio data and the sample rate (Int).
 *                        New data provided here will be processed and added to the visualization.
 *                        Pass null or the same instance to avoid reprocessing if data hasn't changed.
 * @param waveColor The color of the waveform lines. Defaults to a pink accent color.
 * @param strokeWidthPx The width of each waveform line in pixels. Defaults to 20px.
 * @param itemsCount The number of vertical lines to display in the waveform. Defaults to 80.
 * @param amplitudeRangeNormalization The raw average amplitude value that should map to the full height of the canvas.
 *                                    For example, if set to 20f (default), an average amplitude of 20 or -20
 *                                    from the input byte data will span the canvas height.
 */
@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    latestAudioData: Pair<ByteArray, Int>?,
    waveColor: Color = DEFAULT_WAVE_COLOR,
    strokeWidthPx: Float = DEFAULT_STROKE_WIDTH_PX,
    itemsCount: Int = DEFAULT_ITEMS_COUNT,
    amplitudeRangeNormalization: Float = DEFAULT_AMPLITUDE_NORMALIZATION
) {
    // Queue to hold the processed amplitude values for drawing.
    // These are typically averages of byte samples from the input audio data.
    val processedDataQueue = remember { mutableStateListOf<Float>() }

    // Effect to process incoming audio data when `latestAudioData` changes.
    LaunchedEffect(latestAudioData) {
        latestAudioData?.let { (bytes, rate) ->
            if (bytes.isEmpty()) return@LaunchedEffect

            val safeItemsCount = if (itemsCount > 0) itemsCount else 1
            val bytesPerItem = if (rate > 0 && safeItemsCount > 0) rate / safeItemsCount else bytes.size + 1

            if (bytesPerItem <= 0) {
                return@LaunchedEffect
            }

            val newProcessedValues = mutableListOf<Float>()
            var currentSum = 0f
            var countInCurrentSum = 0

            for (i in bytes.indices) {
                currentSum += bytes[i].toFloat()
                countInCurrentSum++
                if (countInCurrentSum == bytesPerItem) {
                    newProcessedValues.add(currentSum / countInCurrentSum)
                    currentSum = 0f
                    countInCurrentSum = 0
                }
            }

            // Add any remaining partial segment
            if (countInCurrentSum > 0) {
                newProcessedValues.add(currentSum / countInCurrentSum)
            }

            processedDataQueue.addAll(newProcessedValues)
        }
    }

    // State to trigger recomposition for the animation (simulating polling from the queue).
    var animationTick by remember { mutableStateOf(0) }

    // Animation timer: periodically triggers recomposition to draw the next frame of the wave.
    // This mimics the original View's `postDelayed({ invalidate() }, 300)` behavior.
    LaunchedEffect(Unit) { // Runs once and repeats.
        while (true) {
            delay(ANIMATION_DELAY_MS)
            animationTick++ // Changing this state triggers recomposition of the Canvas.
        }
    }

    Canvas(modifier = modifier) {
        // Read the tick to ensure this Canvas block recomposes when animationTick changes.
        val currentAnimationTick = animationTick

        val canvasWidth = size.width
        val canvasHeight = size.height
        val safeDrawableItemsCount = if (itemsCount > 0) itemsCount else 1
        val itemVisualWidth = canvasWidth / safeDrawableItemsCount // Width of each segment for a line

        for (i in 0 until safeDrawableItemsCount) {
            val xPosition = itemVisualWidth * i + itemVisualWidth / 2f // Center X for the current line

            // Poll a value from the front of the queue. If empty, use 0f (flat line).
            val rawValue = processedDataQueue.removeFirstOrNull() ?: 0f

            // Normalize the raw amplitude value and calculate line geometry.
            // The original View's logic:
            // effectiveLineHeight = canvasHeight * (rawValue / amplitudeRangeNormalization)
            // y1 = (canvasHeight - effectiveLineHeight) / 2f  (top of the line)
            // y2 = (canvasHeight + effectiveLineHeight) / 2f  (bottom of the line)
            // This centers a line of length `abs(effectiveLineHeight)` at `canvasHeight / 2`.
            val effectiveLineHeight = canvasHeight * (rawValue / amplitudeRangeNormalization)

            val y1 = (canvasHeight - effectiveLineHeight) / 2f
            val y2 = (canvasHeight + effectiveLineHeight) / 2f

            drawLine(
                color = waveColor,
                start = Offset(x = xPosition, y = y1),
                end = Offset(x = xPosition, y = y2),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Butt // Default for Android Paint.StrokeCap is BUTT.
            )
        }
    }
}