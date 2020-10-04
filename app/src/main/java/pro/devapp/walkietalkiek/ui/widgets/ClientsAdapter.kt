package pro.devapp.walkietalkiek.ui.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.modules.data.entities.ClientEntity

class ClientsAdapter : RecyclerView.Adapter<ClientViewHolder>() {
    private val items = ArrayList<ClientEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        return ClientViewHolder(
            LayoutInflater.from(parent.context).inflate(ClientViewHolder.LAYOUT_ID, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    fun setItems(clients: List<ClientEntity>) {
        items.clear()
        items.addAll(clients)
        notifyDataSetChanged()
    }
}