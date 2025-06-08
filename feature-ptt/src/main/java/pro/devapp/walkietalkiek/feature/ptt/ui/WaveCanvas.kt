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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.math.min

@Composable
fun WaveCanvas(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF57C00), // Matches orange theme (secondaryLight)
    data: ByteArray? = null,
    sampleRate: Int = 80
) {
    // State for waveform data
    val queueForChart = remember { LinkedList<Float>() }
    val waveformData = remember { mutableStateListOf<Float>() }

    // Update waveform data when new ByteArray is received
    LaunchedEffect(data, sampleRate) {
        if (data != null) {
            queueForChart.clear()
            for (i in data.indices) {
                queueForChart.add(data[i].toFloat())
            }
            waveformData.clear()
            waveformData.addAll(queueForChart)
        }
    }

    var resetJob: Job? = null
    // Periodic refresh every 300ms
    LaunchedEffect(data) {
        resetJob?.cancel()
        resetJob = launch {
            delay(500)
            waveformData.clear()
        }
    }

    // Convert strokeWidth from px to dp for Compose
    val strokeWidthPx = with(LocalDensity.current) { 1.dp.toPx() }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val itemsCount = min(canvasWidth.toInt(), waveformData.size)

        drawRect(
            color = Color.Transparent,
            size = size
        )

        for (i in 0 until itemsCount) {
            val xOffset = (canvasWidth / itemsCount) * i + canvasWidth / itemsCount
            val value = waveformData.getOrElse(i) { 0f }
            val h = value
            val startY = (canvasHeight - h) / 2

            drawLine(
                color = color,
                start = Offset(xOffset, startY),
                end = Offset(xOffset, startY + h),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round,
                pathEffect = null
            )
        }
    }
}