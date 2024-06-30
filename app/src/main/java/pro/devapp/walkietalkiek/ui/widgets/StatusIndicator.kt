package pro.devapp.walkietalkiek.ui.widgets

import android.content.Context
import android.util.AttributeSet
import pro.devapp.walkietalkiek.R

class StatusIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    companion object {
        val STATE_OFFLINE = intArrayOf(R.attr.state_offline)
        val STATE_ONLINE = intArrayOf(R.attr.state_online)
        val STATE_ACTIVE = intArrayOf(R.attr.state_active)
    }

    var currentStatus: STATES = STATES.STATE_OFFLINE
        set(value) {
            field = value
            refreshDrawableState()
        }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        when (currentStatus) {
            STATES.STATE_OFFLINE -> {
                mergeDrawableStates(drawableState, STATE_OFFLINE)
            }
            STATES.STATE_ONLINE -> {
                mergeDrawableStates(drawableState, STATE_ONLINE)
            }
            STATES.STATE_ACTIVE -> {
                mergeDrawableStates(drawableState, STATE_ACTIVE)
            }
        }
        return drawableState
    }

    enum class STATES {
        STATE_OFFLINE,
        STATE_ONLINE,
        STATE_ACTIVE
    }
}