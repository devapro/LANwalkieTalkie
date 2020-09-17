package pro.devapp.walkietalkiek.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pro.devapp.walkietalkiek.R
import java.util.*

class WaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 20f
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    private val queueForChart = LinkedList<Float>()

    companion object {
        const val itemsCount = 80
    }

    fun setData(bytes: ByteArray, rate: Int) {
        var totalValue = 0f
        val bytesPerItem = rate / itemsCount
        for (i in bytes.indices) {
            totalValue += bytes[i].toFloat()
            if (i % bytesPerItem == 0) {
                queueForChart.add(totalValue / bytesPerItem)
                totalValue = 0f
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0..itemsCount) {
            val xOffset = ((width / itemsCount) * i).toFloat() + width / itemsCount
            val value = queueForChart.poll() ?: 0f
            val h = height.toFloat() * value / 20
            val startY = (height - h) / 2
            canvas.drawLine(x + xOffset, startY, x + xOffset, startY + h, paint)
        }
        postDelayed({ invalidate() }, 300)
    }
}