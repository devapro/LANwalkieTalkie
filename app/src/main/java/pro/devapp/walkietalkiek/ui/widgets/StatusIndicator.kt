package pro.devapp.walkietalkiek.ui.widgets

import android.content.Context
import android.util.AttributeSet
import pro.devapp.walkietalkiek.R

class StatusIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    var currentStatus: STATES? = STATES.STATE_OFFLINE
        set(value) {
            field = value
            refreshDrawableState()
        }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        currentStatus?.let { status ->
            mergeDrawableStates(drawableState, intArrayOf(status.drawable))
        } ?: run {
            mergeDrawableStates(drawableState, intArrayOf(STATES.STATE_OFFLINE.drawable))
        }
        return drawableState
    }

    enum class STATES (val drawable: Int) {
        STATE_OFFLINE(R.attr.state_offline),
        STATE_ONLINE(R.attr.state_online),
        STATE_ACTIVE(R.attr.state_active)
    }
}