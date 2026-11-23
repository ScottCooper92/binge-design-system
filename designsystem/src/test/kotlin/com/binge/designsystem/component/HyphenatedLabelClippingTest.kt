package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A hyphenated label beside a trailing tag, and a tag's own label, never end a line inside a word at any width or font. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HyphenatedLabelClippingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun sweep(content: @Composable () -> Unit) {
        var fontScale by mutableStateOf(1.0f)
        var widthDp by mutableStateOf(360)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme { Box(Modifier.width(widthDp.dp)) { content() } }
            }
        }
        val faults = mutableListOf<String>()
        for (scale in listOf(1.0f, 1.3f)) {
            for (w in listOf(260, 300, 360)) {
                fontScale = scale
                widthDp = w
                rule.waitForIdle()
                runCatching { rule.assertNoTextClipped() }.onFailure { faults += "font $scale, ${w}dp: ${it.message}" }
            }
        }
        assertTrue(faults.joinToString("\n"), faults.isEmpty())
    }

    @Test
    fun `a hyphenated row label beside a tag`() =
        sweep {
            ItemGroup(
                title = null,
                rows = listOf(
                    ListItem(
                        icon = Icons.Filled.Person,
                        label = "Auto-approve",
                        trailingContent = { BingeTag(label = "Seasons") },
                    ),
                ),
            )
        }

    @Test
    fun `a tag in a narrow slot`() = sweep { BingeTag(label = "Seasons") }
}
