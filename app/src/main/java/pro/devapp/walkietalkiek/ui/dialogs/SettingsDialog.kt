package pro.devapp.walkietalkiek.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.devapp.walkietalkiek.databinding.DialogSettingsBinding
import pro.devapp.walkietalkiek.ui.viewBinding

class SettingsDialog : BaseBottomSheetDialog() {
    override val screenBinding by viewBinding(DialogSettingsBinding::inflate)
    override val viewModel by viewModels<SettingsViewModel>() {
        object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    requireActivity().application
                ) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return screenBinding.root
    }
}