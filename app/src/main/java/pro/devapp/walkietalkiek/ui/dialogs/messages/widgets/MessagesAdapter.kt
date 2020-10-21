package pro.devapp.walkietalkiek.ui.dialogs.messages.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.modules.data.entities.MessageEntity

class MessagesAdapter : RecyclerView.Adapter<MessageViewHolder>() {
    private val messages = ArrayList<MessageEntity>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(MessageViewHolder.LAYOUT_ID, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.onBind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun setMessages(newMessages: List<MessageEntity>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}