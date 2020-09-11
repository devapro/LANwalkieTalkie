package pro.devapp.walkietalkiek.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.reactivex.subjects.PublishSubject
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.databinding.ViewBottomButtonsBinding

class BottomButtons @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    val buttonsClickSubject = PublishSubject.create<Buttons>()

    private val viewBinding = ViewBottomButtonsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.view_bottom_buttons, this, true)
    ).apply {
        messages.setOnClickListener { buttonsClickSubject.onNext(Buttons.MESSAGES) }
        settings.setOnClickListener { buttonsClickSubject.onNext(Buttons.SETTINGS) }
        exit.setOnClickListener { buttonsClickSubject.onNext(Buttons.EXIT) }
    }

    enum class Buttons {
        MESSAGES,
        SETTINGS,
        EXIT
    }
}