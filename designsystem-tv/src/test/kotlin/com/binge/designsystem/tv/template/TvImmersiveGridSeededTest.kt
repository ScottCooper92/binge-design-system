package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val COLUMNS = 3
private const val COUNT = 9
private const val SEEDED = 1

/**
 * A seed rings a cell before any cell holds focus, so a hold in front of the grid that has focus is not an invisible one. The
 * cell reports it through `isFocused`, which these tests read as `selected`: a plate while loading, the cell it becomes when
 * the items arrive, and never once a cell really has focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveGridSeededTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var content by mutableStateOf<TvImmersiveGridContent<Int>>(TvImmersiveGridContent.Loading)
    private var seeded by mutableStateOf<Int?>(SEEDED)

    @Test
    fun `a seeded plate draws focused while the cells load`() {
        setGrid()

        composeTestRule.onAllNodesWithTag("plate")[SEEDED].assertIsSelected()
        composeTestRule.onAllNodesWithTag("plate")[0].assertIsNotSelected()
    }

    @Test
    fun `no seed draws no ring`() {
        seeded = null
        setGrid()

        composeTestRule.onAllNodesWithTag("plate")[SEEDED].assertIsNotSelected()
    }

    @Test
    fun `the cell a seeded plate becomes is drawn focused`() {
        setGrid()

        content = TvImmersiveGridContent.Items(count = COUNT, key = { it }, at = { it })
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cell-$SEEDED").assertIsSelected()
        composeTestRule.onNodeWithTag("cell-0").assertIsNotSelected()
    }

    @Test
    fun `real focus takes the ring from the seed`() {
        setGrid()

        composeTestRule.onAllNodesWithTag("plate")[0].requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("plate")[0].assertIsSelected()
        composeTestRule.onAllNodesWithTag("plate")[SEEDED].assertIsNotSelected()
    }

    private fun setGrid() {
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    TvImmersiveGrid(
                        heading = "Heading",
                        content = content,
                        onItemClick = {},
                        artwork = {},
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        columns = COLUMNS,
                        hosting = TvPageHosting.Destination,
                        seededIndex = seeded,
                        inPlace = TvImmersiveGridInPlace<Int>(
                            cell = { item, isFocused, onFocusChanged, onClick, cellModifier ->
                                Box(
                                    cellModifier
                                        .height(24.dp)
                                        .testTag(if (item == null) "plate" else "cell-$item")
                                        .semantics { selected = isFocused }
                                        .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
                                )
                            },
                        ),
                    ) { _, _, _, _, _ -> }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
