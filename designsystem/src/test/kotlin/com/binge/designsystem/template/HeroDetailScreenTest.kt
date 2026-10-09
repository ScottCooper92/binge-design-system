package com.binge.designsystem.template

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A [HeroDetailScreen] page with `contentMaxWidth = Dp.Infinity` lays its sections from the edge, and the reading
 * margin it hands them is measured from the width the page really gets, after anything that insets it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1000dp-h800dp")
class HeroDetailScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun page(
        contentMaxWidth: Dp,
        start: Dp = 0.dp,
        onMargin: (Dp) -> Unit = {},
    ) = rule.setContent {
        BingeExpressiveTheme {
            // The start padding stands in for a side cutout's inset, which a JVM window cannot have.
            Box(Modifier.padding(start = start)) {
                HeroDetailScreen(title = "Title", onBack = null, contentMaxWidth = contentMaxWidth, hero = { Box(Modifier.height(HERO)) }) {
                    onMargin(LocalHeroReadingMargin.current)
                    Text("Short")
                }
            }
        }
    }

    @Test
    fun `an unbounded page starts a narrow section at its edge, not in the middle`() {
        page(contentMaxWidth = Dp.Infinity)

        assertEquals(0.dp, rule.onNodeWithText("Short").getBoundsInRoot().left)
    }

    @Test
    fun `a bounded page still centres its reading column`() {
        page(contentMaxWidth = READING_WIDTH)

        // A column of the text's own width, centred: it starts well right of the edge.
        assertTrue(rule.onNodeWithText("Short").getBoundsInRoot().left > READING_MARGIN_AT_1000)
    }

    @Test
    fun `the reading margin is measured inside the page, after an inset takes its share`() {
        var margin = 0.dp
        page(contentMaxWidth = Dp.Infinity, start = INSET) { margin = it }
        rule.waitForIdle()

        // (1000 - 100 - 640) / 2, not (1000 - 640) / 2 measured from the whole window.
        assertEquals((WINDOW - INSET - READING_WIDTH) / 2, margin)
    }
}

private val HERO = 200.dp
private val WINDOW = 1000.dp
private val INSET = 100.dp

/** The design system's `content_max_width`. */
private val READING_WIDTH = 640.dp
private val READING_MARGIN_AT_1000 = (WINDOW - READING_WIDTH) / 2
