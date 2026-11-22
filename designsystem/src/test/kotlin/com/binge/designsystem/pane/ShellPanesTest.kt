package com.binge.designsystem.pane

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private data class Key(
    val name: String,
) : NavKey

private fun entry(
    name: String,
    metadata: Map<String, Any> = emptyMap(),
    content: @Composable () -> Unit = { Text(name) },
) = NavEntry<NavKey>(Key(name), metadata = metadata) { content() }

private fun ShellPanesStrategy.scene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? =
    with(this) { SceneStrategyScope<NavKey>().calculateScene(entries) }

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp")
class ShellPanesTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val page = entry("page", ShellPaneRole.page(opensItems = true))
    private val item = entry("item", ShellPaneRole.item)
    private var detail: ShellDetail? = null

    private val shell =
        entry("shell", ShellPaneRole.shell) {
            detail = LocalShellDetail.current
            ShellPanes(
                home = { Text("home") },
                defaultPage = { Text("default page") },
                itemPlaceholder = { Text("pick an item") },
            )
        }

    private fun show(strategy: ShellPanesStrategy, vararg above: NavEntry<NavKey>) {
        val scene = checkNotNull(strategy.scene(listOf(shell) + above))
        rule.setContent { TestTheme { scene.content() } }
        rule.waitForIdle()
    }

    @Test
    fun aDisabledStrategyLeavesEveryStackToTheNextOne() {
        assertNull(ShellPanesStrategy(enabled = false).scene(listOf(shell, page)))
    }

    @Test
    fun aStackThatDoesNotStartWithTheShellIsNotItsOwn() {
        assertNull(ShellPanesStrategy(enabled = true).scene(listOf(page, shell)))
    }

    @Test
    fun anEntryAboveTheShellThatIsNotAPageCoversTheWholeWindow() {
        assertNull(ShellPanesStrategy(enabled = true).scene(listOf(shell, entry("settings search"))))
    }

    @Test
    fun theShellAloneIsASceneWithNoPageBesideItsHome() {
        show(ShellPanesStrategy(enabled = true))

        rule.onNodeWithText("home").assertIsDisplayed()
        rule.onNodeWithText("default page").assertIsDisplayed()
        assertNull(detail?.page)
    }

    @Test
    fun aPageOpensBesideTheHome() {
        show(ShellPanesStrategy(enabled = true), page)

        rule.onNodeWithText("home").assertIsDisplayed()
        rule.onNodeWithText("page").assertIsDisplayed()
        assertSame(page, detail?.page)
    }

    @Test
    fun withoutAThirdPaneTheNewestEntryIsTheOneOtherPane() {
        show(ShellPanesStrategy(enabled = true, threePane = false), page, item)

        rule.onNodeWithText("item").assertIsDisplayed()
        assertSame(item, detail?.page)
        assertNull(detail?.item)
        assertFalse(detail?.opensItems == true)
    }

    @Test
    fun withAThirdPaneAPageThatOpensItemsGetsItsItemBesideIt() {
        show(ShellPanesStrategy(enabled = true, threePane = true), page, item)

        rule.onNodeWithText("home").assertIsDisplayed()
        rule.onNodeWithText("page").assertIsDisplayed()
        rule.onNodeWithText("item").assertIsDisplayed()
        assertSame(page, detail?.page)
        assertSame(item, detail?.item)
    }

    @Test
    fun theThirdPaneShowsItsPlaceholderUntilAnItemIsOpen() {
        show(ShellPanesStrategy(enabled = true, threePane = true), page)

        rule.onNodeWithText("pick an item").assertIsDisplayed()
        assertTrue(detail?.opensItems == true)
        assertNotNull(detail?.page)
        assertEquals(null, detail?.item)
    }

    @Test
    fun aPageThatOpensNoItemsKeepsTwoPanesOnAThreePaneWindow() {
        val plain = entry("plain page", ShellPaneRole.page())
        show(ShellPanesStrategy(enabled = true, threePane = true), plain)

        rule.onNodeWithText("plain page").assertIsDisplayed()
        assertFalse(detail?.opensItems == true)
    }

    @Test
    fun theSceneKeepsOneKeyForEveryStackSoAPageSwapsInPlace() {
        val strategy = ShellPanesStrategy(enabled = true)

        assertEquals(strategy.scene(listOf(shell))?.key, strategy.scene(listOf(shell, page))?.key)
    }
}
