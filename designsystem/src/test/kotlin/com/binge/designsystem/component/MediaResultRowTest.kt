package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaResultRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val long =
        MediaResult(
            "k",
            MediaResultKind.Poster,
            null,
            "The Lord of the Rings: The Fellowship of the Ring, Extended Edition",
            "Movie · 2001",
            rating = 8.4f,
            meta = "Adventure, Fantasy, Action",
        )

    private fun show(
        result: MediaResult,
        fontScale: Float = 1f,
        onClick: () -> Unit = {},
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme { Box(Modifier.width(412.dp)) { MediaResultRow(result, onClick = onClick) } }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a tap on the row opens it`() {
        var clicks = 0
        show(long, onClick = { clicks++ })
        rule.onNodeWithText("Movie · 2001").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `a long name and meta are not clipped at 1_3`() {
        show(long, fontScale = 1.3f)
        assertTrue(rule.assertNoTextClipped() >= 2)
    }

    @Test
    fun `a long name and meta are not clipped at 2_0`() {
        show(long, fontScale = 2.0f)
        assertTrue(rule.assertNoTextClipped() >= 2)
    }

    @Test
    fun `a result with only a name draws no status line`() {
        show(MediaResult("k", MediaResultKind.Avatar, null, "Greta Lee"))
        rule.onNodeWithText("Greta Lee").assertExists()
    }

    private fun heightOf(content: @androidx.compose.runtime.Composable () -> Unit): Int {
        rule.setContent { TestTheme { Box(Modifier.width(412.dp)) { content() } } }
        rule.waitForIdle()
        val bounds = rule.onRoot().getBoundsInRoot()
        return (bounds.bottom - bounds.top).value.toInt()
    }

    @Test
    fun `a skeleton is as tall as the row it stands in for, for every kind`() {
        MediaResultKind.entries.forEach { kind ->
            val row = heightOf { MediaResultRow(MediaResult("k", kind, null, "Name", "Subtitle"), onClick = {}) }
            val skeleton = heightOf { MediaResultRowSkeleton(kind = kind) }
            assertEquals("$kind", row, skeleton)
        }
    }
}
