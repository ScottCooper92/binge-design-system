package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MOVIES = "Movies"
private const val ACCOUNT = "Account"

/**
 * A screen reader reads a destination by its label, once, in every presentation and state (#424): an
 * unselected avatar tab whose label is not drawn still has a name, and a selected item whose glyph and label
 * text are both drawn is not read twice. An avatar's initials never stand in for the label.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class NavSuiteItemSemanticsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun render(
        presentation: BingeNavPresentation,
        selectedKey: String,
        style: BingeNavFloatingStyle = BingeNavFloatingStyle.IconWithSelectedLabel,
    ) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeNavSuiteShell(
                    items = listOf(
                        BingeNavSuiteItem(key = "movies", label = MOVIES, icon = Icons.Filled.Movie, testTag = "movies"),
                        BingeNavSuiteItem(
                            key = "account",
                            label = ACCOUNT,
                            icon = Icons.Filled.AccountCircle,
                            avatarName = "Ana Lima",
                            largeAvatar = true,
                            testTag = "account",
                        ),
                    ),
                    selectedKey = selectedKey,
                    onSelect = {},
                    presentation = presentation,
                    floatingStyle = style,
                    content = {},
                )
            }
        }
    }

    /** Everything the item's merged node reads: its descriptions, then its text. */
    private fun spoken(tag: String): List<String> {
        val config = composeTestRule.onNodeWithTag(tag).fetchSemanticsNode().config
        return config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() +
            config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
    }

    @Test
    fun `an unselected icon-only avatar tab is named by its label`() {
        render(BingeNavPresentation.FloatingBar, selectedKey = "movies")

        assertEquals(listOf(ACCOUNT), spoken("account"))
    }

    @Test
    fun `a selected floating item reads its label once`() {
        render(BingeNavPresentation.FloatingBar, selectedKey = "movies")

        assertEquals(listOf(MOVIES), spoken("movies"))
    }

    @Test
    fun `a text-first avatar tab reads its label, not its initials`() {
        render(BingeNavPresentation.FloatingBar, selectedKey = "movies", style = BingeNavFloatingStyle.TextFirst)

        assertEquals(listOf(ACCOUNT), spoken("account"))
    }

    @Test
    fun `a rail item reads its label once`() {
        render(BingeNavPresentation.CustomRail, selectedKey = "movies")

        assertEquals(listOf(MOVIES), spoken("movies"))
        assertEquals(listOf(ACCOUNT), spoken("account"))
    }

    @Test
    fun `a bottom bar item reads its label once`() {
        render(BingeNavPresentation.BottomBar, selectedKey = "movies")

        assertEquals(listOf(MOVIES), spoken("movies"))
        assertEquals(listOf(ACCOUNT), spoken("account"))
    }
}
