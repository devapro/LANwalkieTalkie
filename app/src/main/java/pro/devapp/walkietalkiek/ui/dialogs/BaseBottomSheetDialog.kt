package pro.devapp.walkietalkiek.ui.dialogs

import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.ui.BaseViewModel

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {

    protected abstract val screenBinding: ViewBinding
    protected abstract val viewModel: BaseViewModel

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }


    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(p0: View, p1: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
    }
}