package com.binge.designsystem.viewmodel

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * A map of [ViewModelStore]s by key, kept in a view model so the stores outlive a rotation or a fold, and go when their key does.
 *
 * Hold one in a parent's store and ask it for a [ViewModelStoreOwner] by key. Two screens that ask for the same key share one store,
 * so a list and the item opened from it read and change the same state. A store is cleared by [clear], by [retain] when its key is
 * no longer wanted, or when the holder itself is cleared.
 *
 * @need keep view models by key, beyond a configuration change, for pages that share state
 * @group feedback
 * @replaces a hand-built map of ViewModelStores held in a parent view model
 */
class KeyedViewModelStores<K : Any> : ViewModel() {
    private val stores = mutableMapOf<K, ViewModelStore>()
    private val owners = mutableMapOf<K, ViewModelStoreOwner>()

    /** The store owner for [key], made on first use. Asking again gives the same owner. */
    fun owner(key: K): ViewModelStoreOwner =
        owners.getOrPut(key) {
            val store = storeFor(key)
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = store
            }
        }

    /** The store for [key], made on first use. */
    fun storeFor(key: K): ViewModelStore = stores.getOrPut(key) { ViewModelStore() }

    /** Clears and forgets the store for [key], if there is one. */
    fun clear(key: K) {
        stores.remove(key)?.clear()
        owners.remove(key)
    }

    /** Clears every store whose key is not in [keys]: the pages they belonged to have left. */
    fun retain(keys: Set<K>) {
        (stores.keys - keys).forEach(::clear)
    }

    override fun onCleared() {
        stores.values.forEach { it.clear() }
        stores.clear()
        owners.clear()
    }
}

/**
 * Gives the screens inside [content] view models that last as long as [content] is shown, not the activity.
 *
 * The store is held by a [KeyedViewModelStores] in the parent's store, under [key]. A configuration change disposes the composition
 * without ending the scope, so the store is cleared only when [content] leaves for good, or the parent goes. The parent's factory
 * and creation extras are passed on, so Hilt and saved state work as they would there. A parent that has none, as a preview's has not,
 * gives view models made with their no-argument constructor. Keep one key live in one place at a time: when either scope using it
 * leaves, the store is cleared for both.
 *
 * @need scope the view models of a subtree to a key, so they do not outlive it
 * @group feedback
 * @replaces a hand-built ViewModelStoreOwner provided around a subtree
 */
@Composable
fun KeyedViewModelScope(key: String, content: @Composable () -> Unit) {
    val parent = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner to scope from" }
    val stores =
        viewModel<KeyedViewModelStores<String>>(
            viewModelStoreOwner = parent,
            key = SCOPE_HOLDER_KEY,
            factory = viewModelFactory { initializer { KeyedViewModelStores<String>() } },
        )
    val owner = remember(parent, key) { ScopedStoreOwner(parent, stores.storeFor(key)) }
    val activity = LocalActivity.current
    DisposableEffect(owner) {
        onDispose {
            if (activity?.isChangingConfigurations != true) stores.clear(key)
        }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}

/** Not the default key, which a consumer's own `KeyedViewModelStores` in the same parent would share. */
private const val SCOPE_HOLDER_KEY = "com.binge.designsystem.viewmodel.KeyedViewModelScope"

private class ScopedStoreOwner(
    private val parent: ViewModelStoreOwner,
    override val viewModelStore: ViewModelStore,
) : ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = (parent as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory ?: ViewModelProvider.NewInstanceFactory()

    override val defaultViewModelCreationExtras: CreationExtras
        get() = (parent as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras ?: CreationExtras.Empty
}
