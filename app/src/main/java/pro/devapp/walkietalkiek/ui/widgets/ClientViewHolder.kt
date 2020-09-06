package pro.devapp.walkietalkiek.ui.widgets

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.databinding.ItemClientBinding
import pro.devapp.walkietalkiek.entities.ClientEntity

class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val viewBinding = ItemClientBinding.bind(itemView)
    private val disconnectedStateColor =
        ContextCompat.getColor(itemView.context, R.color.colorPrimaryDark)
    private val connectedStateColor = ContextCompat.getColor(itemView.context, R.color.colorAccent)

    companion object {
        const val LAYOUT_ID = R.layout.item_client
    }

    fun onBind(clientEntity: ClientEntity) {
        viewBinding.name.text = clientEntity.hostAddress
        viewBinding.status.setBackgroundColor(if (clientEntity.isConnected) connectedStateColor else disconnectedStateColor)
    }
}