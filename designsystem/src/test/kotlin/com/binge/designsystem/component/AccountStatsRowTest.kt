package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val STATS =
    listOf(
        AccountStat("42", "Watchlist"),
        AccountStat("186", "Favourites"),
        AccountStat("23", "Rated"),
    )

/** An [AccountStatsRow] keeps every caption whole, in a row at a normal font and stacked at a large one. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is cut.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AccountStatsRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun check(fontScale: Float) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(412.dp)) { AccountStatsRow(stats = STATS) }
                }
            }
        }
        rule.waitForIdle()
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= STATS.size)
    }

    @Test fun `no caption breaks at 1_0`() = check(1f)

    @Test fun `no caption breaks at 1_3`() = check(1.3f)

    @Test fun `no caption breaks at 2_0`() = check(2f)
}
