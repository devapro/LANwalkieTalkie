package pro.devapp.walkietalkiek.ui.widgets.clients

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.modules.data.entities.ClientEntity

class ClientsListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        itemAnimator = DefaultItemAnimator()
        adapter = ClientsAdapter()
    }

    fun setItems(clients: List<ClientEntity>) {
        (adapter as ClientsAdapter).setItems(clients)
    }
}