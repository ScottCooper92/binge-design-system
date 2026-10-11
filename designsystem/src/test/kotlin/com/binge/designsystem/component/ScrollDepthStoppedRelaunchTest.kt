package com.binge.designsystem.component

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

private const val ROW_COUNT = 60
private const val DEEP_INDEX = 40

/**
 * A configuration change that reaches an activity that is already stopped keeps the visit's depth (#503).
 *
 * [ScrollDepthCarryTest] passes the configuration-change check in. This one runs a real activity, so the check is the
 * activity's own and the depth goes through its saved-state bundle. The relaunch is driven step by step in the
 * platform's order: the stop saves the state while no configuration change is under way, and the relaunch destroys the
 * activity as a configuration change and restores the new one from that save, without saving again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ScrollDepthStoppedRelaunchTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    @After
    fun reset() {
        ScrollDepthHostActivity.reset()
    }

    @Test
    fun `a theme change while stopped keeps the depth, and the next visit starts again`() {
        val controller = Robolectric.buildActivity(ScrollDepthHostActivity::class.java).setup()
        rule.waitForIdle()
        scrollTo(DEEP_INDEX)
        val deepest = lastVisibleIndex()
        scrollTo(0)
        val top = lastVisibleIndex()
        assertTrue("the list must scroll past one screen: deepest $deepest, top $top", deepest > top)

        // Backgrounded: the activity stops and saves its state, with no configuration change under way.
        val saved = Bundle()
        controller.pause().stop().saveInstanceState(saved)
        // The theme changes while it is stopped. The platform relaunches it without saving again, because it saved at
        // the stop, so the new activity restores from that one bundle. ActivityController.recreate saves a second time,
        // inside the change, which would hide the gap this pins.
        RuntimeEnvironment.setQualifiers("+night")
        ReflectionHelpers.setField(controller.get(), "mChangingConfigurations", true)
        controller.destroy()
        Robolectric.buildActivity(ScrollDepthHostActivity::class.java).setup(saved)
        rule.waitForIdle()
        assertTrue("reported on the relaunch: ${ScrollDepthHostActivity.reported}", ScrollDepthHostActivity.reported.isEmpty())

        leave()
        assertEquals(listOf<Int?>(deepest), ScrollDepthHostActivity.reported)

        ScrollDepthHostActivity.shown = true
        rule.waitForIdle()
        leave()
        assertEquals(listOf<Int?>(deepest, top), ScrollDepthHostActivity.reported)
    }

    private fun scrollTo(index: Int) {
        rule.runOnIdle { ScrollDepthHostActivity.listState?.requestScrollToItem(index) }
        rule.waitForIdle()
    }

    private fun lastVisibleIndex(): Int =
        rule.runOnIdle {
            checkNotNull(ScrollDepthHostActivity.listState)
                .layoutInfo.visibleItemsInfo
                .last()
                .index
        }

    private fun leave() {
        ScrollDepthHostActivity.shown = false
        rule.waitForIdle()
    }
}

/** Sets its content in [onCreate], so a relaunch composes the list again as a real screen does. */
class ScrollDepthHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (shown) {
                val state = rememberLazyListState()
                listState = state
                ScrollDepthEffect(state) { reported += it }
                LazyColumn(state = state) {
                    items(List(ROW_COUNT) { "row $it" }) { row -> Text(row, Modifier.fillMaxWidth().height(40.dp)) }
                }
            }
        }
    }

    companion object {
        var shown by mutableStateOf(true)
        var listState: LazyListState? = null
        val reported = mutableListOf<Int?>()

        fun reset() {
            shown = true
            listState = null
            reported.clear()
        }
    }
}
