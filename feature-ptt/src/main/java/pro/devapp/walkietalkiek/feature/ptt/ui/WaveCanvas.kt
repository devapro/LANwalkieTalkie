package pro.devapp.walkietalkiek.feature.ptt.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import java.util.LinkedList

@Composable
fun WaveCanvas(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF57C00), // Matches orange theme (secondaryLight)
    strokeWidth: Float = 20f,
    data: ByteArray? = null,
    sampleRate: Int = 80
) {
    // State for waveform data
    val queueForChart = remember { LinkedList<Float>() }
    val waveformData = remember { mutableStateListOf<Float>() }

    // Update waveform data when new ByteArray is received
    LaunchedEffect(data, sampleRate) {
        if (data != null) {
            var totalValue = 0f
            val bytesPerItem = sampleRate / 80 // itemsCount = 80
            for (i in data.indices) {
                totalValue += data[i].toFloat()
                if (i % bytesPerItem == 0) {
                    queueForChart.add(totalValue / bytesPerItem)
                    totalValue = 0f
                }
            }
            // Update waveformData with the latest values
            waveformData.clear()
            waveformData.addAll(queueForChart.take(80))
            queueForChart.clear()
        }
    }

    // Periodic refresh every 300ms
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            // Trigger recomposition by updating waveformData
            waveformData.clear()
            waveformData.addAll(queueForChart.take(80))
            queueForChart.clear()
        }
    }

    // Convert strokeWidth from px to dp for Compose
    val strokeWidthDp = with(LocalDensity.current) { strokeWidth.toDp() }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val itemsCount = 80

        for (i in 0 until itemsCount) {
            val xOffset = (canvasWidth / itemsCount) * i + canvasWidth / itemsCount
            val value = waveformData.getOrElse(i) { 0f }
            val h = canvasHeight * value / 20
            val startY = (canvasHeight - h) / 2

            drawLine(
                color = color,
                start = Offset(xOffset, startY),
                end = Offset(xOffset, startY + h),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = null
            )
        }
    }
}