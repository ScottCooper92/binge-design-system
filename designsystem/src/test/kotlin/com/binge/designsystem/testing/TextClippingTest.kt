package com.binge.designsystem.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.BingeTag
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.MediaTypeTag
import com.binge.designsystem.component.MediaTypeTagType
import com.binge.designsystem.component.WrappingRow
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** [assertNoTextClipped] passes a deliberate ellipsis and a row that fits, and fails the card that was too short. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is clipped.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TextClippingTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = FONT_SCALE)) {
                BingeExpressiveTheme { Box(Modifier.width(ROW_WIDTH)) { content() } }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `an ellipsized one-line text passes`() {
        show {
            Text(
                text = "A title far too long to fit on a single line of this narrow a column, so it ellipsizes",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        rule.assertNoTextClipped()
    }

    /**
     * Simulates the undercount `FlowRow` showed under `ListRow` in a consumer: a wrapping row whose intrinsic height
     * reports fewer lines than it then draws. `ListRow` sizes its card from that report, so the card is too short and
     * the line below is squeezed or clipped. The stand-in is a layout that states the fault directly. `FlowRow` itself
     * did not reproduce it here, with the design system's own tags at widths from 190dp to 330dp and font scale 1.3,
     * so a test of `FlowRow` would pass without proving the assertion catches anything.
     */
    @Test
    fun `a row that reports fewer lines than it draws, under a ListRow, fails and names the clipped text`() {
        show {
            ListRow(
                leading = { Box(Modifier.size(width = LEADING_WIDTH, height = LEADING_HEIGHT)) },
                trailingContent = { Box(Modifier.size(TRAILING_SIZE)) },
            ) {
                Column(it) {
                    Text("A title")
                    UnderReportingStack()
                    Spacer(Modifier.weight(1f).height(1.dp))
                    Text("FROM Scott")
                }
            }
        }

        val failure = runCatching { rule.assertNoTextClipped() }.exceptionOrNull()
        assertTrue("expected the clipped line to be reported", failure is AssertionError)
        assertTrue(failure?.message.orEmpty().contains("FROM Scott"))
    }

    /** The same row at eight widths, because the undercount shows only where the tags wrap onto a further line. */
    private fun showAtManyWidths(statusLine: @Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = FONT_SCALE)) {
                BingeExpressiveTheme {
                    Column {
                        ROW_WIDTHS.forEach { width -> Box(Modifier.width(width.dp)) { RealRow(statusLine) } }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `real tags in a WrappingRow under a ListRow pass at every width`() {
        showAtManyWidths { WrappingRow(horizontalSpacing = CHIP_GAP, verticalSpacing = CHIP_GAP) { Tags() } }

        rule.assertNoTextClipped()
    }

    @Composable
    private fun RealRow(statusLine: @Composable () -> Unit) {
        ListRow(
            leading = { Box(Modifier.size(width = LEADING_WIDTH, height = LEADING_HEIGHT)) },
            trailingContent = { Box(Modifier.size(TRAILING_SIZE)) },
        ) {
            Column(it) {
                Text("The Lord of the Rings: The Rings of Power", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                statusLine()
                Spacer(Modifier.weight(1f).height(1.dp))
                Text("FROM Scott")
            }
        }
    }

    @Composable
    private fun Tags() {
        BingeTag(label = "Partly available")
        MediaTypeTag(type = MediaTypeTagType.Tv)
        Text("4K")
        Text("2022")
    }

    @Test
    fun `text cut off while wholly in view inside a scroll container fails`() {
        show {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Box(Modifier.height(SHORT_BOX).clipToBounds()) { Text("Cut off by its own parent") }
            }
        }

        val failure = runCatching { rule.assertNoTextClipped() }.exceptionOrNull()
        assertTrue("expected the clipped text to be reported", failure is AssertionError)
        assertTrue(failure?.message.orEmpty().contains("Cut off by its own parent"))
    }

    @Test
    fun `text scrolled partly out of a scroll container is skipped`() {
        show {
            Column(Modifier.height(SHORT_BOX).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(SHORT_BOX))
                Text("Scrolled out of view")
            }
        }

        assertEquals(0, rule.assertNoTextClipped())
    }

    /** A chip as a fixed box, so the stack's height is known and does not depend on font metrics. */
    @Composable
    private fun Chip() {
        Box(Modifier.size(width = CHIP_WIDTH, height = CHIP_HEIGHT))
    }

    /** Three chips stacked, whose intrinsic height is one chip's. */
    @Composable
    private fun UnderReportingStack() {
        Layout(
            content = { repeat(STACK_LINES) { Chip() } },
            measurePolicy =
                object : MeasurePolicy {
                    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
                        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
                        val height = placeables.sumOf { it.height }
                        return layout(placeables.maxOf { it.width }, height) {
                            var y = 0
                            placeables.forEach {
                                it.placeRelative(0, y)
                                y += it.height
                            }
                        }
                    }

                    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
                        measurables.first().minIntrinsicHeight(width)

                    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
                        measurables.first().maxIntrinsicHeight(width)
                },
        )
    }

    private companion object {
        val SHORT_BOX = 10.dp
        const val STACK_LINES = 3
        val ROW_WIDTHS = listOf(190, 210, 230, 250, 270, 290, 310, 330)
        val CHIP_WIDTH = 60.dp
        val CHIP_HEIGHT = 24.dp
        val CHIP_GAP = 8.dp
        val LEADING_WIDTH = 50.dp
        val LEADING_HEIGHT = 70.dp
        val TRAILING_SIZE = 24.dp
        val ROW_WIDTH = 260.dp
        const val FONT_SCALE = 1.3f
    }
}
