package pro.devapp.walkietalkiek.ui.dialogs.messages.widgets

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.modules.data.entities.MessageEntity
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.databinding.ItemMessageBinding

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val viewBinding = ItemMessageBinding.bind(itemView)

    companion object {
        const val LAYOUT_ID = R.layout.item_message
    }

    fun onBind(messageEntity: MessageEntity) {
        viewBinding.name.text = messageEntity.from
        viewBinding.text.text = messageEntity.message
    }
}