package com.binge.designsystem.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.LogRowEntry
import com.binge.designsystem.component.LogRowLevel
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val levels = listOf(FilterChipItem("All"), FilterChipItem("Error"), FilterChipItem("Warning"))

private fun rows(count: Int) =
    List(count) {
        LogRowEntry(
            id = "r$it",
            timestampMillis = null,
            timestampRaw = "2026-09-29T21:14:07.000Z",
            level = LogRowLevel.Error,
            label = "Sonarr",
            message = "Unable to reach the instance",
            data = null,
        )
    }

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LogReaderScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        state: LogReaderState,
        callbacks: LogReaderCallbacks = LogReaderCallbacks({}, {}, {}, {}, {}),
        query: String = "",
        fontScale: Float = 1f,
    ) {
        rule.setContent { Frame(state, callbacks, query, fontScale) }
        rule.waitForIdle()
    }

    @androidx.compose.runtime.Composable
    private fun Frame(
        state: LogReaderState,
        callbacks: LogReaderCallbacks,
        query: String,
        fontScale: Float,
    ) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
            TestTheme {
                LogReaderScreen(title = "Logs", levels = levels, selected = 0, query = query, state = state, callbacks = callbacks)
            }
        }
    }

    @Test
    fun `a tap on a level chip reports its index`() {
        var picked = -1
        show(LogReaderState.Ready(rows(3), hasMore = false), LogReaderCallbacks({ picked = it }, {}, {}, {}, {}))
        rule.onNodeWithText("Error").performClick()
        assertEquals(1, picked)
    }

    @Test
    fun `typing in the field reports the text`() {
        var typed = ""
        show(LogReaderState.Ready(rows(3), hasMore = false), LogReaderCallbacks({}, { typed = it }, {}, {}, {}))
        rule.onNode(hasSetTextAction()).performTextInput("timeout")
        assertEquals("timeout", typed)
    }

    @Test
    fun `an empty state keeps the chips and the field`() {
        show(LogReaderState.Empty(message = "Nothing matches."), query = "zzz")
        rule.onNodeWithText("Nothing matches.").assertIsDisplayed()
        rule.onNodeWithText("Error").assertIsDisplayed()
    }

    @Test
    fun `a failed state offers Try again`() {
        var retried = 0
        show(LogReaderState.Failed(ErrorKind.Network), LogReaderCallbacks({}, {}, {}, { retried++ }, {}))
        rule.onNodeWithText("Try again").performClick()
        assertEquals(1, retried)
    }

    @Test
    fun `the footer shows only while appending or after a failed append`() {
        assertFalse(showsLogReaderFooter(LogReaderState.Ready(rows(2), hasMore = true)))
        assertTrue(showsLogReaderFooter(LogReaderState.Ready(rows(2), hasMore = true, appending = true)))
        assertTrue(showsLogReaderFooter(LogReaderState.Ready(rows(2), hasMore = true, appendFailed = true)))
    }

    @Test
    fun `a short list that has more asks for the next page once`() {
        var asked = 0
        show(LogReaderState.Ready(rows(3), hasMore = true), LogReaderCallbacks({}, {}, { asked++ }, {}, {}))
        assertEquals(1, asked)
    }

    private fun skeletonReservesRow(fontScale: Float) {
        rule.assertSkeletonReservesGeometry(
            anchors = listOf(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
            checkStartEdge = true,
            checkSize = true,
        ) { resolved ->
            Frame(
                state = if (resolved) LogReaderState.Ready(rows(3), hasMore = false) else LogReaderState.Loading,
                callbacks = LogReaderCallbacks({}, {}, {}, {}, {}),
                query = "",
                fontScale = fontScale,
            )
        }
    }

    @Test fun `the skeleton reserves the first row's position and size at 1x`() = skeletonReservesRow(1f)

    @Test fun `the skeleton reserves the first row's position and size at 2x`() = skeletonReservesRow(2f)

    private fun clipping(fontScale: Float) {
        show(LogReaderState.Ready(rows(3), hasMore = false), query = "timeout", fontScale = fontScale)
        val checked = rule.assertNoTextClipped()
        assertTrue("only $checked text nodes were checked", checked >= 3)
    }

    @Test fun `chips, field and rows are not clipped at 1_3`() = clipping(1.3f)

    @Test fun `chips, field and rows are not clipped at 2_0`() = clipping(2.0f)
}
