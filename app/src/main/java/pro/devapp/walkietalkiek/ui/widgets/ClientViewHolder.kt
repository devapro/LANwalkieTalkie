package pro.devapp.walkietalkiek.ui.widgets

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.databinding.ItemClientBinding
import pro.devapp.walkietalkiek.entities.ClientEntity
import java.util.*

class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val viewBinding = ItemClientBinding.bind(itemView)

    companion object {
        val LAYOUT_ID = R.layout.item_client
    }

    fun onBind(clientEntity: ClientEntity) {
        viewBinding.name.text = clientEntity.hostAddress
        displayStatus(clientEntity)
    }

    private fun displayStatus(clientEntity: ClientEntity) {
        val currentDate = Date().time
        when {
            currentDate - clientEntity.lastDataReceivedAt < 1000 && clientEntity.isConnected -> {
                viewBinding.status.currentStatus = StatusIndicator.STATES.STATE_ACTIVE
            }
            clientEntity.isConnected -> {
                viewBinding.status.currentStatus = StatusIndicator.STATES.STATE_ONLINE
            }
            else -> {
                viewBinding.status.currentStatus = StatusIndicator.STATES.STATE_OFFLINE
            }
        }

        //TODO
        viewBinding.status.postDelayed({ displayStatus(clientEntity) }, 500)
    }
}