package pro.devapp.walkietalkiek.ui.widgets

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.RelativeLayout
import io.reactivex.subjects.PublishSubject
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.databinding.ViewPptButtonBinding

@SuppressLint("ClickableViewAccessibility")
class PPTButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val viewBinding = ViewPptButtonBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.view_ppt_button, this, true)
    )
    val pushStateSubject = PublishSubject.create<Boolean>()

    private val scaleDownAnimator = ObjectAnimator.ofPropertyValuesHolder(
        viewBinding.pulseBg,
        PropertyValuesHolder.ofFloat("scaleX", 2f),
        PropertyValuesHolder.ofFloat("scaleY", 2f)
    )
        .apply {
            duration = PULSE_ANIMATION_DURATION
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

    companion object {
        const val PULSE_ANIMATION_DURATION = 600L
    }

    init {
        viewBinding.buttonBg.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pulseAnimationStart()
                    pushStateSubject.onNext(true)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    pulseAnimationStop()
                    pushStateSubject.onNext(false)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun pulseAnimationStart() {
        scaleDownAnimator.start()
    }

    private fun pulseAnimationStop() {
        scaleDownAnimator.cancel()
        viewBinding.pulseBg.scaleX = 1f
        viewBinding.pulseBg.scaleY = 1f
    }
}