package com.binge.designsystem.component

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [BackdropTile] and [LogoTile]: one click target, one accessibility item announcing the name, and the name when there is no logo. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CategoryTileTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    @Test
    fun `a backdrop tile is one click target announcing its title`() {
        var clicks = 0
        rule.setContent { TestTheme { BackdropTile(title = "Action", imageUrl = null, onClick = { clicks++ }) } }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Action").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a backdrop tile announces its description when it differs from the title`() {
        rule.setContent {
            TestTheme { BackdropTile(title = "Sci-Fi", imageUrl = null, onClick = {}, description = "Science Fiction") }
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Science Fiction")
    }

    @Test
    fun `a backdrop tile with its title hidden draws no text and still announces the title`() {
        var clicks = 0
        rule.setContent {
            TestTheme { BackdropTile(title = "Action", imageUrl = "sample://backdrop", onClick = { clicks++ }, showTitle = false) }
        }

        rule.onAllNodesWithText("Action", useUnmergedTree = true).assertCountEquals(0)
        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Action").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a backdrop tile with its title hidden announces its description when given one`() {
        rule.setContent {
            TestTheme { BackdropTile(title = "Sci-Fi", imageUrl = null, onClick = {}, description = "Science Fiction", showTitle = false) }
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Science Fiction")
    }

    @Test
    fun `a logo tile with no logo shows its name and is one click target`() {
        var clicks = 0
        rule.setContent { TestTheme { LogoTile(name = "Example Studio", logoUrl = null, onClick = { clicks++ }) } }

        rule.onAllNodesWithText("Example Studio", useUnmergedTree = true).assertCountEquals(1)
        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Example Studio").performClick()

        assertEquals(1, clicks)
    }
}
