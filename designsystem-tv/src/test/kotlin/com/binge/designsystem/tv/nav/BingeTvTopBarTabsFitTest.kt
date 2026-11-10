package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.binge.designsystem.tv.R as TvR

/**
 * A tab's label is never cut while the bar has room for it. With a trailing slot the tabs lose the width the slot takes, but they keep
 * their own: each tab is laid out at least as wide as its label and its padding, at 1x and 2x font, and clear of the trailing items.
 * Compared on layout size against the label's measured width, not on whether text reports overflow.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvTopBarTabsFitTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var labels = listOf("Home", "Movies", "Search")

    /** Each label's measured width, and the horizontal padding a tab adds either side of it, both in px, read in composition. */
    private val labelWidths = mutableMapOf<String, Int>()
    private var paddingPx = 0f

    private fun setBar(fontScale: Float, tabLabels: List<String> = labels) {
        labels = tabLabels
        composeTestRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale)) {
                BingeTvTheme {
                    val measurer = rememberTextMeasurer()
                    val style = MaterialTheme.typography.titleMedium
                    labels.forEach { labelWidths[it] = measurer.measure(it, style, softWrap = false, maxLines = 1).size.width }
                    paddingPx = with(LocalDensity.current) { dimensionResource(TvR.dimen.tv_top_bar_item_padding_horizontal).toPx() }
                    BingeTvTopBar(
                        header = TvNavItem(
                            key = "account",
                            label = "Profile",
                            icon = Icons.Filled.Person,
                            displayName = "Alexander Hamilton",
                        ),
                        items = labels.map { TvNavItem(key = it, label = it, icon = Icons.Filled.Home) },
                        footer = null,
                        selectedKey = "home",
                        onSelect = {},
                        itemFocusRequesters = emptyMap(),
                        down = remember { FocusRequester() },
                        onFocusedChange = {},
                        trailing = {
                            TvIconButton(icon = Icons.Filled.Person, label = "Account", onClick = {})
                            TvIconButton(icon = Icons.Filled.Settings, label = "Settings", onClick = {})
                        },
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun assertNoTabIsCut() {
        labels.forEach { label ->
            val laidOut = composeTestRule
                .onNodeWithContentDescription(label)
                .fetchSemanticsNode()
                .layoutInfo.width
            val needed = labelWidths.getValue(label) + 2 * paddingPx
            assertTrue("$label is laid out $laidOut px wide, its label and padding need ${needed.toInt()}", laidOut >= needed - 1)
        }
    }

    private fun assertTabsClearOfTrailing() {
        val lastTab = composeTestRule.onNodeWithContentDescription(labels.last()).getBoundsInRoot()
        val firstTrailing = composeTestRule.onNodeWithContentDescription("Account").getBoundsInRoot()
        assertTrue("The tabs run under the trailing items", lastTab.right <= firstTrailing.left)
    }

    @Test
    fun `tab labels are not cut with a trailing slot at 1x font, though the slot takes room from a crowded bar`() {
        setBar(fontScale = 1f, tabLabels = listOf("Home", "Movies", "TV shows", "Requests", "Library", "Search"))
        assertNoTabIsCut()
        assertTabsClearOfTrailing()
    }

    @Test
    fun `tab labels are not cut with a trailing slot at 2x font`() {
        setBar(fontScale = 2f)
        assertNoTabIsCut()
        assertTabsClearOfTrailing()
    }
}
