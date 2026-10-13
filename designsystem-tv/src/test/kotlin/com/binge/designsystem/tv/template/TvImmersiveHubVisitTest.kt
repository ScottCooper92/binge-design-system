package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val HERO_TARGET = "hero-target"
private const val ITEMS_PER_ROW = 3

private val Rows =
    listOf(
        TvHubRow(key = "first", title = "First", items = List(ITEMS_PER_ROW) { it }),
        TvHubRow(key = "second", title = "Second", items = List(ITEMS_PER_ROW) { 10 + it }),
    )

/**
 * The hub reports where focus went, for impressions: a row when focus arrives in it, never one that is only in the
 * viewport, a click's row and position, and once as the hub leaves, the deepest place focus reached. That place
 * counts the hero as 0, so with a hero the first row is 1; a dropped hero offset would report the hero and the
 * first row alike.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubVisitTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager
    private var attached by mutableStateOf(true)
    private val visible = mutableListOf<String>()
    private val tapped = mutableListOf<Pair<String, Int>>()
    private val deepest = mutableListOf<Int?>()

    @Test
    fun `focus arriving on the hero reports the hero and no row`() {
        setHub(withHero = true)
        enterFromRail()

        assertEquals(listOf("hero"), visible.distinct())
    }

    @Test
    fun `focus arriving in a row reports that row only`() {
        setHub(withHero = true)
        enterFromRail()
        move(FocusDirection.Down)

        assertEquals(listOf("hero", "first"), visible.distinct())
    }

    @Test
    fun `a click reports its row and its position in the row`() {
        setHub(withHero = false)
        enterFromRail()
        move(FocusDirection.Down)
        move(FocusDirection.Right)
        composeTestRule.onNodeWithTag("card-11").assertIsFocused().performClick()

        assertEquals(listOf("second" to 1), tapped)
    }

    @Test
    fun `leaving on the hero reports depth zero`() {
        setHub(withHero = true)
        enterFromRail()
        leave()

        assertEquals(listOf<Int?>(0), deepest)
    }

    @Test
    fun `with a hero the first row is depth one`() {
        setHub(withHero = true)
        enterFromRail()
        move(FocusDirection.Down)
        leave()

        assertEquals(listOf<Int?>(1), deepest)
    }

    @Test
    fun `without a hero the rows count from zero, and moving back up keeps the deepest`() {
        setHub(withHero = false)
        enterFromRail()
        move(FocusDirection.Down)
        move(FocusDirection.Up)
        leave()

        assertEquals(listOf<Int?>(1), deepest)
    }

    @Test
    fun `a visit that never took focus reports no depth`() {
        setHub(withHero = true)
        leave()

        assertEquals(listOf<Int?>(null), deepest)
        assertTrue(visible.isEmpty())
    }

    private fun setHub(withHero: Boolean) {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    if (attached) {
                        TvImmersiveHub(
                            rows = Rows,
                            itemId = { it },
                            cardWidth = 96.dp,
                            onItemClick = {},
                            artwork = {},
                            copy = {},
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            hero =
                                if (withHero) {
                                    { Box(Modifier.size(width = 320.dp, height = 96.dp).testTag(HERO_TARGET).focusable()) }
                                } else {
                                    null
                                },
                            hosting = TvPageHosting.RailDestination,
                            onHeroVisible = { visible += "hero" },
                            onRowVisible = { visible += it },
                            onItemTapped = { row, index -> tapped += row to index },
                            onVisitEnded = { deepest += it },
                        ) { item, _, onFocusChanged, onClick, cellModifier ->
                            Card(item, onFocusChanged, onClick, cellModifier)
                        }
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun leave() {
        attached = false
        composeTestRule.waitForIdle()
    }

    private fun enterFromRail() = move(FocusDirection.Right)

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
        composeTestRule.waitForIdle()
    }
}

@Composable
private fun Card(
    item: Int,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.aspectRatio(2f / 3f).testTag("card-$item").tvClickable(onFocusChanged = onFocusChanged, onClick = onClick))
}
