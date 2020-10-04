package pro.devapp.walkietalkiek.ui

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (LayoutInflater) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                this@FragmentViewBindingDelegate.binding = null
            }
        })

        return viewBindingFactory(fragment.layoutInflater).also {
            this@FragmentViewBindingDelegate.binding = it
        }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (LayoutInflater) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)