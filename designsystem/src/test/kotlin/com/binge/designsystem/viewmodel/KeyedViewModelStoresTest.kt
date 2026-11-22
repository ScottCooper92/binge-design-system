package com.binge.designsystem.viewmodel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A view model that says when it was cleared. */
private class Probe : ViewModel() {
    var cleared = false

    override fun onCleared() {
        cleared = true
    }
}

private fun KeyedViewModelStores<String>.probe(key: String): Probe =
    ViewModelProvider.create(
        owner(key),
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: kotlin.reflect.KClass<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras,
            ): T = Probe() as T
        },
    )[Probe::class]

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class KeyedViewModelStoresTest {
    @get:Rule
    val rule = createTakeDownAndroidComposeRule<ComponentActivity>()

    private val stores = KeyedViewModelStores<String>()

    @Test
    fun theSameKeyGivesTheSameOwnerAndAnotherKeyAnotherOne() {
        assertSame(stores.owner("a"), stores.owner("a"))
        assertNotSame(stores.owner("a"), stores.owner("b"))
    }

    @Test
    fun twoScreensAskingForOneKeyShareTheViewModel() {
        assertSame(stores.probe("a"), stores.probe("a"))
        assertNotSame(stores.probe("a"), stores.probe("b"))
    }

    @Test
    fun clearEndsOnlyThatKeysViewModels() {
        val a = stores.probe("a")
        val b = stores.probe("b")

        stores.clear("a")

        assertTrue(a.cleared)
        assertFalse(b.cleared)
        assertNotSame("a new store after a clear", a, stores.probe("a"))
    }

    @Test
    fun retainClearsEveryKeyNotInTheSet() {
        val a = stores.probe("a")
        val b = stores.probe("b")
        val c = stores.probe("c")

        stores.retain(setOf("b"))

        assertTrue(a.cleared)
        assertFalse(b.cleared)
        assertTrue(c.cleared)
    }

    @Test
    fun theHoldersOwnEndClearsEverything() {
        val parent = ViewModelStore()
        val held = ViewModelProvider.create(
            parent,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: kotlin.reflect.KClass<T>,
                    extras: androidx.lifecycle.viewmodel.CreationExtras,
                ): T = KeyedViewModelStores<String>() as T
            },
        )[KeyedViewModelStores::class] as KeyedViewModelStores<String>
        val a = held.probe("a")
        val b = held.probe("b")

        parent.clear()

        assertTrue(a.cleared && b.cleared)
    }

    private var key by mutableStateOf<String?>("a")
    private val seen = mutableMapOf<String, Probe>()

    private fun showScoped() {
        rule.setContent {
            TestTheme {
                key?.let { current ->
                    KeyedViewModelScope(current) { seen[current] = viewModel<Probe>(factory = probeFactory) }
                }
            }
        }
        rule.waitForIdle()
    }

    private val probeFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T =
            Probe() as T
    }

    @Test
    fun aScopeHoldsItsViewModelUntilItsKeyChanges() {
        showScoped()
        val first = seen.getValue("a")
        assertFalse(first.cleared)

        key = "b"
        rule.waitForIdle()

        assertTrue("the old key's view model goes with it", first.cleared)
        assertFalse(seen.getValue("b").cleared)
        assertEquals(setOf("a", "b"), seen.keys)
    }

    @Test
    fun aScopeThatLeavesTheCompositionClearsItsViewModel() {
        showScoped()
        val first = seen.getValue("a")

        key = null
        rule.waitForIdle()

        assertTrue(first.cleared)
    }

    @Test
    fun aParentWithNoFactoryStillGivesNoArgumentViewModels() {
        val plain = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        var made: NoArgModel? = null
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides plain) {
                KeyedViewModelScope("a") { made = viewModel<NoArgModel>() }
            }
        }
        rule.waitForIdle()

        assertTrue(made != null)
    }
}

class NoArgModel : ViewModel()
