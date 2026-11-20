package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ENTRY = "entry"

/**
 * The detail page's tabs, one focus stop each: the carousel steps with ←/→ and OK opens the item in the slot, the gallery's entry
 * lands on its first image and then on the last one focused. What each opens is
 * observed through the click callback, since the images under test are not text.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailTabsFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run (#2350, #2367). */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()

    @Test
    fun `the carousel steps along its items and OK opens the one in the slot`() {
        setContent {
            TvDetailSlotCarousel(
                items = listOf(10, 11, 12),
                key = { it },
                aspect = 1f,
                slotHeight = SLOT,
                stripHeight = STRIP,
                onClick = { opened += it },
                image = {},
                caption = {},
            )
        }
        enter()
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionLeft)
        press(Key.DirectionCenter)

        assertEquals(listOf(1), opened)
    }

    @Test
    fun `the gallery's entry lands on the first image, then on the last one focused`() {
        val images = List(IMAGES) { TvDetailGalleryImage("u$it", portrait = false, index = 100 + it) }
        setContent { TvDetailGalleryGrid(images = images, onOpen = { opened += it }) }
        enter()
        press(Key.DirectionCenter)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(100, 102), opened)

        composeTestRule.onNodeWithTag(ENTRY).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
        press(Key.DirectionCenter)

        assertEquals(102, opened.last())
    }

    @Test
    fun `the cast's See all card follows the last person, and OK opens it`() {
        val people = List(2) { TvCastCell(key = "cast-$it", name = "Actor $it", role = "Role $it", profileUrl = null) }
        var seeAll = 0
        setContent { TvDetailCast(people = people, onCastClick = { opened += it }, onSeeAll = { seeAll++ }) }
        enter()
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        // The one focused node reads as the card, not as the last person the slot keeps.
        composeTestRule.onNodeWithContentDescription("See all").assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(1, seeAll)
        assertEquals(emptyList<Int>(), opened)
    }

    @Test
    fun `a cast tab with only a director who has no photo still reaches See all`() {
        val people = emptyList<TvCastCell>()
        var seeAll = 0
        setContent { TvDetailCast(people = people, onCastClick = { opened += it }, onSeeAll = { seeAll++ }) }
        enter()

        composeTestRule.onNodeWithContentDescription("See all").assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(1, seeAll)
        assertEquals(emptyList<Int>(), opened)
    }

    private fun setContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        composeTestRule.setContent {
            BingeTvTheme {
                androidx.compose.foundation.layout.Column {
                    Box(Modifier.testTag(ENTRY).size(ENTRY_SIZE).focusable())
                    content()
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun enter() {
        composeTestRule.onNodeWithTag(ENTRY).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocusable() and isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val ENTRY_SIZE = 20.dp
        val SLOT = 120.dp
        val STRIP = 80.dp
        const val IMAGES = 6
    }
}
