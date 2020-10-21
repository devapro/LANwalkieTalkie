package pro.devapp.walkietalkiek.ui.dialogs.messages.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.modules.data.entities.MessageEntity

class MessagesList @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, true)
        itemAnimator = DefaultItemAnimator()
        adapter = MessagesAdapter()
    }

    fun setItems(newMessages: List<MessageEntity>) {
        (adapter as MessagesAdapter).setMessages(newMessages)
    }
}