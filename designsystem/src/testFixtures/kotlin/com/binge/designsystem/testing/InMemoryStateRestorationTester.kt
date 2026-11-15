package com.binge.designsystem.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * [androidx.compose.ui.test.junit4.StateRestorationTester] without the `Bundle` and `Parcel` round-trip, for a test of what a
 * configuration change keeps (#1106).
 *
 * The framework's tester encodes the saved state into a `Bundle`, writes it to a `Parcel`, and reads it back. On Android 14,
 * which Robolectric runs as written, a `Bundle` read from a `Parcel` holds a value that needs class loading (a `Serializable`,
 * a `Parcelable`, so an `ArrayList` too) as a lazy value, and holds the parcel itself only through a `WeakReference`. A garbage
 * collection that lands just after the last lazy value is read clears that reference, and the platform throws `Parcel recycled
 * earlier than expected`. That is a rare, timing-dependent failure in the platform's own code, in whichever test happened to run.
 *
 * This tester does what the framework's does except that: it disposes the content, keeps what the registry saved in memory, and
 * composes the content again under a new registry restored from it. The registry still refuses a value the platform could not
 * put in a `Bundle` (its `canBeSaved`), so a state that would not survive a real recreation still fails here. What it does not do
 * is copy the values, so an object a test saved by reference and then changes is the same object after the restore.
 */
class InMemoryStateRestorationTester(
    private val composeTestRule: ComposeContentTestRule,
) {
    private var registry: RestorationRegistry? = null

    @Composable
    private fun InjectRestorationRegistry(content: @Composable () -> Unit) {
        val original =
            requireNotNull(LocalSaveableStateRegistry.current) {
                "InMemoryStateRestorationTester requires a LocalSaveableStateRegistry to be provided"
            }
        val restoration = remember { RestorationRegistry(original) }
        registry = restoration
        CompositionLocalProvider(LocalSaveableStateRegistry provides restoration) {
            if (restoration.shouldEmitChildren) content()
        }
    }

    /** Sets [content] under a registry that this tester can save and restore. */
    fun setContent(content: @Composable () -> Unit) {
        composeTestRule.setContent { InjectRestorationRegistry(content) }
    }

    /** Saves what the content's registry holds, disposes the content, and composes it again from what was saved. */
    fun emulateSavedInstanceStateRestore() {
        val restoration = requireNotNull(registry) { "setContent must be called before emulateSavedInstanceStateRestore" }
        composeTestRule.runOnIdle { restoration.saveStateAndDisposeChildren() }
        composeTestRule.runOnIdle { restoration.emitChildrenWithRestoredState() }
    }

    private class RestorationRegistry(
        private val original: SaveableStateRegistry,
    ) : SaveableStateRegistry {
        var shouldEmitChildren by mutableStateOf(true)
            private set

        private var currentRegistry: SaveableStateRegistry = original
        private var saved: Map<String, List<Any?>>? = null

        override fun consumeRestored(key: String): Any? = currentRegistry.consumeRestored(key)

        override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry =
            currentRegistry.registerProvider(key, valueProvider)

        override fun canBeSaved(value: Any): Boolean = currentRegistry.canBeSaved(value)

        override fun performSave(): Map<String, List<Any?>> = currentRegistry.performSave()

        fun saveStateAndDisposeChildren() {
            saved = currentRegistry.performSave()
            shouldEmitChildren = false
        }

        fun emitChildrenWithRestoredState() {
            currentRegistry = SaveableStateRegistry(saved, original::canBeSaved)
            shouldEmitChildren = true
        }
    }
}
