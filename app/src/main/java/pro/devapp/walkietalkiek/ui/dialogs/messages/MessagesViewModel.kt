package pro.devapp.walkietalkiek.ui.dialogs.messages

import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import pro.devapp.modules.data.entities.MessageEntity
import pro.devapp.modules.storage.DeviceInfoRepository
import pro.devapp.modules.storage.MessagesRepository
import pro.devapp.walkietalkiek.ui.BaseViewModel
import java.util.*

class MessagesViewModel(
    application: Application,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val messagesRepository: MessagesRepository
) : BaseViewModel(application) {
    val messages = MutableLiveData<List<MessageEntity>>()
    private val compositeDisposable = CompositeDisposable()

    init {
        messagesRepository.getMessages().subscribe {
            messages.postValue(it)
        }.apply {
            compositeDisposable.add(this)
        }
    }

    fun sendMessage(text: String) {
        val currentIp = deviceInfoRepository.getCurrentIp()
        currentIp?.let {
            val messageEntity = MessageEntity(Date().time, currentIp, text)
            messagesRepository.addMessage(messageEntity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}