package com.binge.designsystem.template

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.InMemoryStateRestorationTester
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The lazy form of [HeroDetailShortPageTest]: a lazy hero page with little under its hero can still scroll the whole hero away, so the
 * bar's hand-over can finish, and it holds there once the hero has left the list's visible items.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroDetailLazyShortPageTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var state: LazyListState? = null
    private var heroPx = 0
    private var windowHeight by mutableStateOf(450.dp)

    private fun show(
        items: Int,
        itemHeight: Int = 40,
        spacing: Int = 0,
        bottomPadding: Int = 0,
        leading: Int? = null,
    ) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val listState = rememberLazyListState()
                state = listState
                heroPx = with(LocalDensity.current) { HERO_HEIGHT.toPx() }.toInt()
                HeroDetailLazyScreen(
                    title = "A title",
                    onBack = {},
                    listState = listState,
                    heroHeight = HERO_HEIGHT,
                    verticalArrangement = Arrangement.spacedBy(spacing.dp),
                    contentPadding = PaddingValues(bottom = bottomPadding.dp),
                    hero = { Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(Color.DarkGray)) },
                ) {
                    leading?.let { item { Box(Modifier.fillMaxWidth().height(it.dp)) } }
                    items(items) { Box(Modifier.fillMaxWidth().height(itemHeight.dp)) { Text("Item $it") } }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun scrollToEnd() {
        repeat(3) { rule.runOnIdle { runBlocking { state!!.scrollBy(100_000f) } } }
        rule.waitForIdle()
    }

    private fun heroScrolledOut() = state!!.firstVisibleItemIndex > 0 || state!!.firstVisibleItemScrollOffset >= heroPx

    @Test
    fun `a lazy page with one short item can scroll the whole hero away`() {
        show(items = 1)
        scrollToEnd()

        assertTrue("the hero rests at ${state!!.firstVisibleItemScrollOffset} of $heroPx px", heroScrolledOut())
    }

    @Test
    fun `a lazy page with no items can scroll the whole hero away`() {
        show(items = 0)
        scrollToEnd()

        assertTrue(heroScrolledOut())
    }

    @Test
    fun `the offset holds after the hero has left the visible items and the page recomposes`() {
        show(items = 1)
        scrollToEnd()
        val index = state!!.firstVisibleItemIndex
        val offset = state!!.firstVisibleItemScrollOffset

        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()

        assertEquals(index, state!!.firstVisibleItemIndex)
        assertEquals(offset, state!!.firstVisibleItemScrollOffset)
        assertTrue(heroScrolledOut())
        assertTrue(!state!!.canScrollForward)
    }

    @Test
    fun `a small first item pushed above the viewport does not collapse the fill`() {
        show(items = 1, spacing = 16, bottomPadding = 24, leading = 8)
        scrollToEnd()
        val index = state!!.firstVisibleItemIndex
        val offset = state!!.firstVisibleItemScrollOffset

        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()

        assertTrue("the hero rests at $index/$offset of $heroPx px", heroScrolledOut())
        assertEquals(index, state!!.firstVisibleItemIndex)
        assertEquals(offset, state!!.firstVisibleItemScrollOffset)
    }

    @Test
    fun `a page scrolled to its end keeps the hero away across a saved state restore`() {
        val restoration = InMemoryStateRestorationTester(rule)
        restoration.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val listState = rememberLazyListState()
                state = listState
                heroPx = with(LocalDensity.current) { HERO_HEIGHT.toPx() }.toInt()
                HeroDetailLazyScreen(
                    title = "A title",
                    onBack = {},
                    listState = listState,
                    heroHeight = HERO_HEIGHT,
                    hero = { Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(Color.DarkGray)) },
                ) {
                    item { Box(Modifier.fillMaxWidth().height(40.dp)) { Text("Item") } }
                }
            }
        }
        rule.waitForIdle()
        scrollToEnd()
        assertTrue(heroScrolledOut())

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertTrue("the hero is back at ${state!!.firstVisibleItemIndex}/${state!!.firstVisibleItemScrollOffset}", heroScrolledOut())
    }

    private fun setSizedContent(setContent: (@androidx.compose.runtime.Composable () -> Unit) -> Unit) {
        setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val listState = rememberLazyListState()
                state = listState
                heroPx = with(LocalDensity.current) { HERO_HEIGHT.toPx() }.toInt()
                Box(Modifier.height(windowHeight)) {
                    HeroDetailLazyScreen(
                        title = "A title",
                        onBack = {},
                        listState = listState,
                        heroHeight = HERO_HEIGHT,
                        hero = { Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(Color.DarkGray)) },
                    ) {
                        item { Box(Modifier.fillMaxWidth().height(40.dp)) { Text("Item") } }
                    }
                }
            }
        }
    }

    @Test
    fun `a page scrolled to its end keeps the hero away when the window grows`() {
        setSizedContent { rule.setContent(it) }
        rule.waitForIdle()
        scrollToEnd()
        assertTrue(heroScrolledOut())

        windowHeight = 850.dp
        rule.waitForIdle()

        assertTrue("the hero is back at ${state!!.firstVisibleItemIndex}/${state!!.firstVisibleItemScrollOffset}", heroScrolledOut())
    }

    @Test
    fun `a page scrolled to its end keeps the hero away when restored into a taller window`() {
        val restoration = InMemoryStateRestorationTester(rule)
        setSizedContent { restoration.setContent(it) }
        rule.waitForIdle()
        scrollToEnd()
        assertTrue(heroScrolledOut())

        windowHeight = 850.dp
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertTrue("the hero is back at ${state!!.firstVisibleItemIndex}/${state!!.firstVisibleItemScrollOffset}", heroScrolledOut())
    }

    @Test
    fun `a long list is not given extra room`() {
        show(items = 40, itemHeight = 200)
        scrollToEnd()

        val last = state!!.layoutInfo.visibleItemsInfo.last()
        assertEquals(state!!.layoutInfo.totalItemsCount - 1, last.index)
        assertEquals(state!!.layoutInfo.viewportEndOffset, last.offset + last.size)
        assertTrue(!state!!.canScrollForward)
    }

    private companion object {
        val HERO_HEIGHT = 300.dp
    }
}
