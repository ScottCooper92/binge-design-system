package com.binge.designsystem.component

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import java.time.Duration

/**
 * What a sheet's content saves must come back when the activity is recreated, however the sheet is drawn (jellyfin #301).
 * The sheet sits open above `rememberSaveable`d state of the test's own; one value inside its content is changed; the activity is
 * recreated for real, so a window's saved state is the thing under test and not a composition restorer.
 *
 * It drives an `ActivityController` and no compose test rule, on purpose: the rule's environment gives the recreated
 * activity a different positional key for every `rememberSaveable` without a key, which is how a `Dialog` finds its own saved
 * state, so a sheet could never come back under it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
class BingeAdaptiveSheetRecreationTest {
    private var write: (Int) -> Unit = {}
    private var seen = 0
    private var opened: ActivityController<ComponentActivity>? = null

    @After
    fun tearDown() {
        opened?.destroy()
        shadowOf(Looper.getMainLooper()).idle()
        opened = null
    }

    /** Frames run on the Choreographer, whose pace another test in the module may have changed; give them time rather than a bare idle. */
    private fun settle() {
        Snapshot.sendApplyNotifications()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(SETTLE_MILLIS))
    }

    private fun open(sheet: @Composable (@Composable () -> Unit) -> Unit): ActivityController<ComponentActivity> {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        opened = controller
        show(controller, sheet)
        return controller
    }

    private fun show(controller: ActivityController<ComponentActivity>, sheet: @Composable (@Composable () -> Unit) -> Unit) {
        controller.get().setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                var open by rememberSaveable { mutableStateOf(true) }
                if (open) sheet { SavedValue() }
            }
        }
        settle()
    }

    @Composable
    private fun SavedValue() {
        var value by rememberSaveable { mutableIntStateOf(0) }
        seen = value
        write = { value = it }
    }

    private fun assertValueSurvivesRecreation(rotateTo: String? = null, sheet: @Composable (@Composable () -> Unit) -> Unit) {
        val controller = open(sheet)
        assertEquals(0, seen)

        write(7)
        Snapshot.sendApplyNotifications()
        settle()
        assertEquals(7, seen)

        rotateTo?.let { RuntimeEnvironment.setQualifiers(it) }
        controller.recreate()
        seen = -1
        show(controller, sheet)

        assertEquals("the value a sheet's content saved did not come back", 7, seen)
    }

    @Test
    fun `control, content with no window keeps its saved state across recreation`() {
        assertValueSurvivesRecreation { content -> Column { content() } }
    }

    @Test
    fun `a Compose Dialog keeps its content's saved state across recreation`() {
        assertValueSurvivesRecreation { content ->
            androidx.compose.ui.window
                .Dialog(onDismissRequest = {}) { content() }
        }
    }

    @Test
    fun `a bottom sheet keeps its content's saved state across recreation`() {
        assertValueSurvivesRecreation { content -> BingeBottomSheet(onDismissRequest = {}) { content() } }
    }

    @Test
    fun `an adaptive task sheet keeps its content's saved state across recreation`() {
        assertValueSurvivesRecreation { content ->
            BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}) { content() }
        }
    }

    @Test
    @Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h800dp")
    fun `an adaptive task sheet drawn as a side sheet keeps its content's saved state across recreation`() {
        assertValueSurvivesRecreation { content ->
            BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}) { content() }
        }
    }

    @Test
    fun `a task sheet that turns from a bottom sheet to a side sheet on rotation keeps its content's saved state`() {
        assertValueSurvivesRecreation(rotateTo = "w900dp-h412dp") { content ->
            BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}) { content() }
        }
    }

    @Test
    @Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h412dp")
    fun `a task sheet that turns from a side sheet to a bottom sheet on rotation keeps its content's saved state`() {
        assertValueSurvivesRecreation(rotateTo = "w412dp-h900dp") { content ->
            BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}) { content() }
        }
    }

    @Test
    fun `a sheet whose root changes does not bring the old stack's page state back`() {
        var root by mutableStateOf("a")
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        controller.get().setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}, root = root) { SavedValue() }
            }
        }
        settle()
        write(7)
        settle()
        assertEquals(7, seen)

        root = "b"
        settle()
        root = "a"
        settle()

        assertEquals("a page of a stack that was replaced came back with its old state", 0, seen)
    }
}

private const val SETTLE_MILLIS = 500L
