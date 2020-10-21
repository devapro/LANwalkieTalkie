package pro.devapp.walkietalkiek.ui.dialogs.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import pro.devapp.walkietalkiek.databinding.DialogMessagesBinding
import pro.devapp.walkietalkiek.ui.MainActivity
import pro.devapp.walkietalkiek.ui.dialogs.BaseBottomSheetDialog
import pro.devapp.walkietalkiek.ui.viewBinding
import java.nio.ByteBuffer

class MessagesDialog : BaseBottomSheetDialog() {

    override val screenBinding by viewBinding(DialogMessagesBinding::inflate)
    override lateinit var viewModel: MessagesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(
            this,
            (requireActivity() as MainActivity).viewModelFactory
        )[MessagesViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screenBinding.send.setOnClickListener {
            val text = screenBinding.messageText.text
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text.toString())
                (requireActivity() as MainActivity).sendMessage(
                    ByteBuffer.wrap(
                        text.toString().toByteArray()
                    )
                )
                screenBinding.messageText.setText("")
            }
        }
        viewModel.messages.observe(viewLifecycleOwner) {
            screenBinding.messagesList.setItems(it)
        }

        return screenBinding.root
    }
}