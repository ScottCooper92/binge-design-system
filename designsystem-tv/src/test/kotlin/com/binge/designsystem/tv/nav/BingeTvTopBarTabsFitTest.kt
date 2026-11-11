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
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
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

    private fun setBar(
        fontScale: Float,
        tabLabels: List<String> = labels,
        expandGear: Boolean = false,
        withHeader: Boolean = true,
        withTrailing: Boolean = true,
    ) {
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
                        ).takeIf { withHeader },
                        items = labels.map { TvNavItem(key = it, label = it, icon = Icons.Filled.Home) },
                        footer = null,
                        selectedKey = "home",
                        onSelect = {},
                        itemFocusRequesters = emptyMap(),
                        down = remember { FocusRequester() },
                        onFocusedChange = {},
                        trailing = if (withTrailing) {
                            {
                                TvIconButton(icon = Icons.Filled.Person, label = "Account", onClick = {})
                                TvIconButton(icon = Icons.Filled.Settings, label = "Settings", onClick = {}, expandOnFocus = expandGear)
                            }
                        } else {
                            null
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

    private fun layoutWidths(): List<Int> =
        labels.map {
            composeTestRule
                .onNodeWithContentDescription(it)
                .fetchSemanticsNode()
                .layoutInfo.width
        }

    private fun avatarLeft() = composeTestRule.onNodeWithContentDescription("Account").getUnclippedBoundsInRoot().left

    private fun assertFocusingTheGearChangesNothing(fontScale: Float, expandGear: Boolean = false) {
        setBar(fontScale = fontScale, expandGear = expandGear)
        val widthsAtRest = layoutWidths()
        val avatarAtRest = avatarLeft()

        composeTestRule.onNodeWithContentDescription("Settings").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsFocused()

        assertEquals("The tabs changed width when the gear took focus", widthsAtRest, layoutWidths())
        assertEquals("The avatar moved when the gear took focus", avatarAtRest.value, avatarLeft().value, 0.5f)
        assertNoTabIsCut()
    }

    @Test
    fun `focusing the gear changes neither the tabs nor the avatar at 1x font`() {
        assertFocusingTheGearChangesNothing(fontScale = 1f)
    }

    @Test
    fun `focusing the gear changes neither the tabs nor the avatar at 2x font`() {
        assertFocusingTheGearChangesNothing(fontScale = 2f)
    }

    @Test
    fun `the bar's avatar is 36dp and the bar is as tall as its tabs`() {
        setBar(fontScale = 1f)
        composeTestRule.onNodeWithContentDescription("Profile").assertWidthIsEqualTo(36.dp).assertHeightIsEqualTo(36.dp)
        composeTestRule.onNodeWithContentDescription("Home").assertHeightIsEqualTo(52.dp)
    }

    private val fiveTabs = listOf("Home", "Movies", "TV shows", "Requests", "Search")

    /** The private app's bar: the account avatar at the start and five tabs, nothing at the end. Fits at every font scale up to 2x. */
    private fun assertHeaderAndFiveTabsAreWhole(fontScale: Float) {
        setBar(fontScale = fontScale, tabLabels = fiveTabs, withTrailing = false)
        assertNoTabIsCut()
    }

    @Test
    fun `a start avatar and five tabs are whole at 1x font`() = assertHeaderAndFiveTabsAreWhole(1f)

    @Test
    fun `a start avatar and five tabs are whole at 1_3x font`() = assertHeaderAndFiveTabsAreWhole(1.3f)

    @Test
    fun `a start avatar and five tabs are whole at 2x font`() = assertHeaderAndFiveTabsAreWhole(2f)

    /** binge-seerr's bar: an avatar and a gear at the end and no tabs. Both stay wholly inside the bar at every font scale. */
    private fun assertTrailingOnlyIsInsideTheBar(fontScale: Float) {
        setBar(fontScale = fontScale, tabLabels = emptyList(), withHeader = false)
        val root = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        listOf("Account", "Settings").forEach { name ->
            val bounds = composeTestRule.onNodeWithContentDescription(name).getUnclippedBoundsInRoot()
            assertTrue("$name runs past the bar's right edge: ${bounds.right} of ${root.right}", bounds.right <= root.right)
            assertTrue("$name starts left of the bar: ${bounds.left}", bounds.left >= root.left)
        }
    }

    @Test
    fun `a trailing avatar and gear with no tabs sit inside the bar at 1x font`() = assertTrailingOnlyIsInsideTheBar(1f)

    @Test
    fun `a trailing avatar and gear with no tabs sit inside the bar at 1_3x font`() = assertTrailingOnlyIsInsideTheBar(1.3f)

    @Test
    fun `a trailing avatar and gear with no tabs sit inside the bar at 2x font`() = assertTrailingOnlyIsInsideTheBar(2f)

    /** The limit the bar's KDoc states: five tabs beside a start avatar and a trailing avatar and gear are whole up to 1.3x font. */
    @Test
    fun `five tabs beside a start avatar and a trailing avatar and gear are whole at 1_3x font`() {
        setBar(fontScale = 1.3f, tabLabels = fiveTabs)
        assertNoTabIsCut()
        assertTabsClearOfTrailing()
    }
}
