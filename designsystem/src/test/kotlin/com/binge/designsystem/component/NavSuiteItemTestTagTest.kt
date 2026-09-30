package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A destination's [BingeNavSuiteItem.testTag] must reach its clickable node in every presentation, because a
 * UI-automation driver finds a destination by that tag and taps it. A tag that landed on the icon or the
 * label instead would resolve but not select, which is exactly the failure a text lookup had.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class NavSuiteItemTestTagTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val selected = mutableListOf<Any>()

    private fun render(presentation: BingeNavPresentation) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeNavSuiteShell(
                    items = listOf(
                        BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie, testTag = "nav-movies"),
                        BingeNavSuiteItem(key = "search", label = "Search", icon = Icons.Filled.Search),
                    ),
                    selectedKey = "search",
                    onSelect = { selected += it },
                    presentation = presentation,
                    content = {},
                )
            }
        }
    }

    private fun assertTagSelects() {
        composeTestRule.onNodeWithTag("nav-movies").assertExists().performClick()
        assertEquals(listOf<Any>("movies"), selected)
    }

    @Test
    fun `the floating bar tags the item's clickable node`() {
        render(BingeNavPresentation.FloatingBar)
        assertTagSelects()
    }

    @Test
    fun `the bottom bar tags the item's clickable node`() {
        render(BingeNavPresentation.BottomBar)
        assertTagSelects()
    }

    @Test
    fun `the custom rail tags the item's clickable node`() {
        render(BingeNavPresentation.CustomRail)
        assertTagSelects()
    }
}
