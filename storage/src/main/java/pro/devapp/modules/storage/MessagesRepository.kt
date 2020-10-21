package pro.devapp.modules.storage

import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import pro.devapp.modules.data.entities.MessageEntity

class MessagesRepository {
    private val messages = ArrayList<MessageEntity>()
    private val messagesSubject = ReplaySubject.createWithSize<List<MessageEntity>>(1)
        .apply {
            subscribeOn(
                Schedulers.io()
            )
        }

    fun addMessage(message: MessageEntity) {
        messages.add(message)
        messagesSubject.onNext(messages)
    }

    fun getMessages(): Subject<List<MessageEntity>> {
        return messagesSubject
    }
}