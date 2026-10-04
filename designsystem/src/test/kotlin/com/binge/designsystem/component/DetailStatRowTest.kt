package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [DetailStatRow]'s `contentPadding`: by default it insets itself like a page, and a caller that is
 * already padded (a card) can ask for none, so the row is not indented twice.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailStatRowTest {
    @get:Rule
    val rule = createComposeRule()

    private val stats = (1..4).map { DetailStat(Icons.Filled.Star, "v$it", "label $it") }

    private fun firstValueCentreFraction(contentPadding: PaddingValues?): Float {
        rule.setContent {
            BingeExpressiveTheme {
                val modifier = Modifier.width(400.dp).testTag("row")
                if (contentPadding == null) {
                    DetailStatRow(stats = stats, modifier = modifier)
                } else {
                    DetailStatRow(stats = stats, modifier = modifier, contentPadding = contentPadding)
                }
            }
        }
        val row = rule.onNodeWithTag("row").getBoundsInRoot()
        val value = rule.onNodeWithText("v1").getBoundsInRoot()
        return (((value.left + value.right) / 2) - row.left) / row.width
    }

    @Test
    fun `with no content padding the first of four cells is centred an eighth of the way across`() {
        val fraction = firstValueCentreFraction(PaddingValues())

        assertEquals(0.125f, fraction, 0.01f)
    }

    @Test
    fun `by default the row insets itself, so its first cell sits further in`() {
        val fraction = firstValueCentreFraction(contentPadding = null)

        assertTrue("expected an inset, got $fraction", fraction > 0.135f)
    }
}
