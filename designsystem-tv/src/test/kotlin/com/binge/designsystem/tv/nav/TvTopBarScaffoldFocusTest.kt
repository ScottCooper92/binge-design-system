package com.binge.designsystem.tv.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

private const val CONTENT = "content"
private const val PAGE = "page"
private const val ACCOUNT = "Account"
private const val SEARCH = "Search"
private const val MOVIES = "Movies"
private const val SHOWS = "TV shows"
private const val SETTINGS = "Settings"
private const val MY_LISTS = "My lists"
private val CONTENT_COLOUR = Color.Magenta
private const val SIGN_OUT = "Sign out"
private const val CANCEL = "Cancel"
private const val SIGN_OUT_TITLE = "Sign out?"

/** Three frames, about 48 ms: less than the 50 ms the hand-off used to wait before it first asked for the content. */
private const val FRAMES_UNDER_OLD_DELAY = 3

/** The 52dp icon-button diameter the items used to be stretched to. */
private val MIN_ITEM_HEIGHT = 52.dp

/**
 * The top bar's focus contract: tabs commit on click rather than on focus, Down reaches the content, Back climbs from the
 * content to the bar and then home, and re-entering the bar lands on the tab it was left on. The scaffold is composed on its
 * own over a single focusable content box. Back goes through a navigation-event dispatcher, the one `BackHandler` registers with
 * when no activity hosts the content; a Back no handler takes reaches its fallback, which is how "left to the system" is observed.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTopBarScaffoldFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var systemBacks = 0
    private val dispatcher = NavigationEventDispatcher(OnBackCompletedFallback { systemBacks++ })
    private val backInput = DirectNavigationEventInput().also { dispatcher.addInput(it) }
    private val selected = mutableStateOf<Any>(KEY_MOVIES)
    private val selections = mutableListOf<Any>()
    private var wentHome = 0
    private val signedInNow = mutableStateOf(false)

    @Test
    fun `startup focus lands in the content`() {
        setBar()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    @Test
    fun `back from the content lands on the selected tab`() {
        setBar()
        pressBack()
        composeTestRule.onNodeWithContentDescription(MOVIES).assertIsFocused()
    }

    @Test
    fun `down from a tab reaches the content`() {
        setBar()
        pressBack()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    @Test
    fun `by default the page sits below the bar and the top inset is zero`() {
        setBar()

        assertTrue("The page starts under the bar", pageTop() > 0f)
        assertEquals(0f, pageInset.value, 0f)
    }

    @Test
    fun `behind the bar the page fills the screen and the bar's height is the top inset`() {
        setBar(behindBar = true)

        assertEquals(0f, pageTop(), 0.5f)
        val tabBottom = composeTestRule
            .onNodeWithContentDescription(MOVIES)
            .getUnclippedBoundsInRoot()
            .bottom.value
        assertTrue("The inset clears the tabs", pageInset.value >= tabBottom)
    }

    @Test
    fun `behind the bar focus keeps its rules, page first then up to the bar and down again`() {
        setBar(behindBar = true)
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        press(Key.DirectionUp)
        composeTestRule.onNodeWithContentDescription(MOVIES).assertIsFocused()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    private var pageInset: Dp = Dp.Unspecified

    private fun pageTop() =
        composeTestRule
            .onNodeWithTag(PAGE)
            .getUnclippedBoundsInRoot()
            .top.value

    @Test
    fun `an icon-only item is a square sized by its padding, not stretched to a minimum height`() {
        setBar()
        val bounds = composeTestRule.onNodeWithContentDescription(SEARCH).getUnclippedBoundsInRoot()
        assertEquals(bounds.width.value, bounds.height.value, 0.5f)
        assertTrue("Height ${bounds.height} should hug the icon", bounds.height < MIN_ITEM_HEIGHT)
    }

    @Test
    fun `walking the tabs selects nothing until one is opened`() {
        setBar()
        pressBack()
        press(Key.DirectionRight)
        composeTestRule.onNodeWithContentDescription(SHOWS).assertIsFocused()
        assertTrue("Focus alone must not commit a tab", selections.isEmpty())
        press(Key.DirectionCenter)
        assertEquals(listOf<Any>(KEY_TV), selections)
        composeTestRule.onNodeWithContentDescription(SHOWS).assertIsSelected()
    }

    /**
     * The destinations are tabs, so the accessibility bridge reports their selection as `selected`, which UiAutomator's
     * `By.selected` and TalkBack read; on a button it would be reported as `checked` (#3994). The gear selects nothing.
     */
    @Test
    fun `the destinations are tabs and the gear is a button with no selection`() {
        setBar()

        composeTestRule
            .onNodeWithContentDescription(MOVIES)
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        composeTestRule
            .onNodeWithContentDescription(ACCOUNT)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        composeTestRule
            .onNodeWithContentDescription(SETTINGS)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }

    @Test
    fun `re-entering the bar lands on the tab it was left on, not the selected one`() {
        setBar()
        pressBack()
        press(Key.DirectionRight)
        press(Key.DirectionDown)
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
        pressBack()
        composeTestRule.onNodeWithContentDescription(SHOWS).assertIsFocused()
        composeTestRule.onNodeWithContentDescription(MOVIES).assertIsSelected()
    }

    @Test
    fun `a selection made elsewhere wins over the tab the bar was left on`() {
        setBar()
        pressBack()
        press(Key.DirectionLeft)
        press(Key.DirectionDown)
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
        selected.value = KEY_TV
        composeTestRule.waitForIdle()
        pressBack()
        composeTestRule.onNodeWithContentDescription(SHOWS).assertIsFocused()
    }

    @Test
    fun `the avatar and the gear are reachable from the tabs`() {
        setBar()
        pressBack()
        press(Key.DirectionLeft)
        press(Key.DirectionLeft)
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        composeTestRule.onNodeWithContentDescription(SETTINGS).assertIsFocused()
    }

    @Test
    fun `back on a tab off home goes home and focuses the home tab`() {
        selected.value = KEY_TV
        setBar()
        pressBack()
        composeTestRule.onNodeWithContentDescription(SHOWS).assertIsFocused()
        pressBack()
        assertEquals(1, wentHome)
        composeTestRule.onNodeWithContentDescription(MOVIES).assertIsFocused()
    }

    @Test
    fun `back on the home tab is left to the system`() {
        setBar()
        pressBack()
        composeTestRule.onNodeWithContentDescription(MOVIES).assertIsFocused()
        pressBack()
        assertEquals("Back on the home tab must fall through to the system", 1, systemBacks)
    }

    /** #4519: the avatar opens the menu by being focused, so Back there closes the menu alone, and the tab stays. */
    @Test
    fun `back with the menu open on the avatar closes only the menu, off the home tab`() {
        selected.value = KEY_TV
        setBar(signedIn = true)
        focusAvatarFrom(KEY_TV)
        composeTestRule.onNodeWithText(MY_LISTS).assertExists()

        pressBack()

        composeTestRule.onNodeWithText(MY_LISTS).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()
        assertEquals("the tab must not change", KEY_TV, selected.value)
        assertEquals("Back must not go home while it only closes the menu", 0, wentHome)

        pressBack()

        assertEquals("a second Back follows the shell's rule", 1, wentHome)
    }

    @Test
    fun `back with the menu open on the avatar closes only the menu, on the home tab, and does not leave`() {
        setBar(signedIn = true)
        focusAvatarFrom(KEY_MOVIES)
        composeTestRule.onNodeWithText(MY_LISTS).assertExists()

        pressBack()

        composeTestRule.onNodeWithText(MY_LISTS).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()
        assertEquals("closing the menu must not leave the app", 0, systemBacks)

        pressBack()

        assertEquals("a second Back on the home tab is left to the system", 1, systemBacks)
    }

    @Test
    fun `back from inside the menu closes it onto the avatar and the tab stays`() {
        selected.value = KEY_TV
        setBar(signedIn = true)
        focusAvatarFrom(KEY_TV)
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()

        pressBack()

        composeTestRule.onNodeWithText(MY_LISTS).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()
        assertEquals(KEY_TV, selected.value)
        assertEquals(0, wentHome)
    }

    @Test
    fun `signed out, back on the avatar off the home tab still goes home`() {
        selected.value = KEY_TV
        setBar()
        focusAvatarFrom(KEY_TV)

        pressBack()

        assertEquals(1, wentHome)
    }

    @Test
    fun `signed out, OK on the avatar opens sign-in`() {
        setBar()
        pressBack()
        press(Key.DirectionLeft)
        press(Key.DirectionLeft)
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()
        press(Key.DirectionCenter)
        assertEquals(listOf<Any>(KEY_ACCOUNT), selections)
    }

    @Test
    fun `signed in, OK on the avatar enters the menu and does not open Account`() {
        setBar(signedIn = true)
        focusAvatar()
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()

        press(Key.DirectionCenter)

        assertTrue("The avatar is a label: OK must not open Account", selections.isEmpty())
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()
    }

    @Test
    fun `signed in, My lists is the way to Account`() {
        setBar(signedIn = true)
        focusAvatar()

        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(listOf<Any>(KEY_ACCOUNT), selections)
    }

    /** The destination's content is rebuilt when Account is chosen, as the shell's pane is, so its arrival target is placed afresh. */
    @Test
    fun `My lists from another tab puts focus in Account's content`() {
        setBar(signedIn = true, remountContent = true)
        focusAvatar()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()

        press(Key.DirectionCenter)

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /** Account already showing: nothing is placed afresh, so there is no arrival to claim, and the request goes to the content directly. */
    @Test
    fun `My lists with Account already showing puts focus in its content with no wait`() {
        selected.value = KEY_ACCOUNT
        setBar(signedIn = true, remountContent = true)
        focusAvatar()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionCenter) }
        repeat(FRAMES_UNDER_OLD_DELAY) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /** The hand-off ends once focus has left the menu, so the avatar opens it again; a hand-off that never ended would keep it shut. */
    @Test
    fun `the avatar's menu opens again after My lists`() {
        setBar(signedIn = true, remountContent = true)
        focusAvatar()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()
        press(Key.DirectionCenter)
        composeTestRule.onNodeWithText(MY_LISTS).assertDoesNotExist()

        focusAvatar()

        composeTestRule.onNodeWithText(MY_LISTS).assertExists()
    }

    @Test
    fun `right out of My lists folds the menu and focuses the first tab`() {
        setBar(signedIn = true)
        focusAvatar()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(MY_LISTS).assertIsFocused()

        press(Key.DirectionRight)

        assertMenuFoldedOntoFirstTab()
    }

    @Test
    fun `right out of Sign out folds the menu and focuses the first tab`() {
        setBar(signedIn = true)
        focusAvatar()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(SIGN_OUT) and isFocusable()).assertIsFocused()

        press(Key.DirectionRight)

        assertMenuFoldedOntoFirstTab()
    }

    private fun assertMenuFoldedOntoFirstTab() {
        composeTestRule.onNodeWithText(MY_LISTS).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(SEARCH).assertIsFocused()
    }

    /** Cancel on the sign-out confirm puts focus back on Sign out, the row that raised it, with the menu still open (#4089). */
    @Test
    fun `cancelling the sign-out confirm returns focus to Sign out`() {
        setBar(signedIn = true)
        openSignOutConfirm()

        composeTestRule.onNode(hasText(CANCEL) and isFocusable()).performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        assertSignOutRefocused()
    }

    /** Back on the sign-out confirm returns to Sign out too: Back goes back where it left (#4089). */
    @Test
    fun `back on the sign-out confirm returns focus to Sign out`() {
        setBar(signedIn = true)
        openSignOutConfirm()

        composeTestRule.runOnUiThread { ShadowDialog.getLatestDialog().onBackPressed() }
        composeTestRule.waitForIdle()

        assertSignOutRefocused()
    }

    @Test
    fun `signed in, the bar is as tall as signed out`() {
        setBar()
        val signedOutTop = composeTestRule.onNodeWithTag(CONTENT).getUnclippedBoundsInRoot().top
        signedInNow.value = true
        composeTestRule.waitForIdle()

        val signedInTop = composeTestRule.onNodeWithTag(CONTENT).getUnclippedBoundsInRoot().top
        assertEquals("the content starts where it did, so the bar kept its height", signedOutTop.value, signedInTop.value, 0.5f)
    }

    @Test
    fun `signed in, the avatar sits where the signed-out one does`() {
        setBar()
        val signedOut = composeTestRule.onNodeWithContentDescription(ACCOUNT).getUnclippedBoundsInRoot()
        signedInNow.value = true
        composeTestRule.waitForIdle()

        val signedIn = composeTestRule.onNodeWithContentDescription(ACCOUNT).getUnclippedBoundsInRoot()
        val outCentre = signedOut.left.value + signedOut.width.value / 2
        val inCentre = signedIn.left.value + signedIn.width.value / 2
        assertEquals("the avatar's centre from the edge", outCentre, inCentre, 0.5f)
    }

    @Test
    fun `the open menu's rows are on screen, below the bar and above the content`() {
        setBar(signedIn = true)
        val contentTop = composeTestRule.onNodeWithTag(CONTENT).getUnclippedBoundsInRoot().top
        focusAvatar()
        press(Key.DirectionCenter)

        listOf(MY_LISTS, SIGN_OUT).forEach { label ->
            val row = composeTestRule.onNode(hasText(label) and isFocusable())
            row.assertIsDisplayed()
            assertTrue("$label sits below the bar", row.getUnclippedBoundsInRoot().top >= contentTop - 1.dp)
        }
        assertNotOccluded(MY_LISTS)
    }

    @Test
    fun `signed in, the avatar reads as selected only while Account is the destination`() {
        setBar(signedIn = true)
        val before = composeTestRule
            .onNodeWithContentDescription(
                ACCOUNT,
            ).fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Selected)
        assertNotEquals(true, before)

        selected.value = KEY_ACCOUNT
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsSelected()
    }

    @Test
    fun `a confirmed sign-out leaves focus on the avatar without another key press`() {
        setBar(signedIn = true)
        openSignOutConfirm()

        composeTestRule.onAllNodes(hasText(SIGN_OUT) and isFocusable())[1].performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(SIGN_OUT_TITLE).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsFocused()
    }

    /** The content paints [CONTENT_COLOUR] over its box, so a pixel of that colour inside the row's bounds means the content is drawn over the menu. */
    private fun assertNotOccluded(label: String) {
        val bounds = composeTestRule.onNode(hasText(label) and isFocusable()).getBoundsInRoot()
        val image = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val density = composeTestRule.density
        val x = with(density) { (bounds.left + bounds.width / 2).roundToPx() }
        val y = with(density) { (bounds.top + bounds.height / 2).roundToPx() }
        assertNotEquals("$label is drawn under the content", CONTENT_COLOUR.toArgb(), image.getPixel(x, y))
    }

    private fun openSignOutConfirm() {
        focusAvatar()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(SIGN_OUT) and isFocusable()).assertIsFocused()
        press(Key.DirectionCenter)
        composeTestRule.onNode(hasText(CANCEL) and isFocusable()).assertIsFocused()
    }

    /** The page reports focus while the sheet is up, so the proof is the confirm gone, Sign out focused, and OK on it raising the confirm again. */
    private fun assertSignOutRefocused() {
        composeTestRule.onNodeWithText(SIGN_OUT_TITLE).assertDoesNotExist()
        composeTestRule.onNodeWithText(MY_LISTS).assertExists()
        val signOut = composeTestRule.onNode(hasText(SIGN_OUT) and isFocusable())
        signOut.assertIsFocused()
        composeTestRule.onNodeWithContentDescription(ACCOUNT).assertIsNotFocused()
        signOut.performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText(CANCEL) and isFocusable()).assertIsFocused()
    }

    /** The bar's tabs run Search, Movies, TV shows after the avatar, so [from] says how far Left has to go. */
    private fun focusAvatarFrom(from: Any) {
        pressBack()
        repeat(if (from == KEY_TV) 3 else 2) { press(Key.DirectionLeft) }
    }

    private fun focusAvatar() {
        pressBack()
        press(Key.DirectionLeft)
        press(Key.DirectionLeft)
    }

    private fun press(key: Key) {
        composeTestRule
            .onNode(
                androidx.compose.ui.test
                    .isFocused(),
            ).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread {
            backInput.backStarted(NavigationEvent(touchX = 0f, touchY = 0f, progress = 0f, swipeEdge = NavigationEvent.EDGE_NONE))
            backInput.backCompleted()
        }
        composeTestRule.waitForIdle()
    }

    private fun setBar(
        signedIn: Boolean = false,
        remountContent: Boolean = false,
        behindBar: Boolean = false,
    ) {
        signedInNow.value = signedIn
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                BingeTvTheme {
                    val content = remember { FocusRequester() }
                    var selectedKey by selected
                    TvTopBarScaffold(
                        slots = TvTopBarSlots(
                            header = TvNavItem(
                                KEY_ACCOUNT,
                                ACCOUNT,
                                Icons.Filled.Person,
                                displayName = if (signedInNow.value) "Glurt" else null,
                            ),
                            items = listOf(
                                TvNavItem(KEY_SEARCH, SEARCH, Icons.Filled.Search),
                                TvNavItem(KEY_MOVIES, MOVIES, Icons.Filled.Movie),
                                TvNavItem(KEY_TV, SHOWS, Icons.Filled.Tv),
                            ),
                            footer = TvNavItem(KEY_SETTINGS, SETTINGS, Icons.Filled.Settings),
                        ),
                        selectedKey = selectedKey,
                        homeKey = KEY_MOVIES,
                        onSelect = {
                            selections += it
                            selectedKey = it
                        },
                        contentDepth = 1,
                        onGoHome = {
                            wentHome++
                            selectedKey = KEY_MOVIES
                        },
                        overlayOpen = false,
                        accountRows = accountRows(
                            selectedKey = selectedKey,
                            onSelect = {
                                selections += it
                                selectedKey = it
                            },
                            contentFocusRequester = content,
                            onSignOut = { signedInNow.value = false },
                        ),
                        contentFocusRequester = content,
                        iconOnlyKeys = setOf(KEY_SEARCH),
                        contentBehindBar = behindBar,
                    ) {
                        pageInset = LocalTvTopInset.current
                        key(if (remountContent) selectedKey else Unit) {
                            Box(Modifier.fillMaxSize().testTag(PAGE)) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(top = pageInset)
                                        .testTag(CONTENT)
                                        .background(CONTENT_COLOUR)
                                        .shellArrivalTarget()
                                        .focusRequester(content)
                                        .focusable(),
                                )
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}

/** The avatar menu's rows as the Binge app builds them, so the scaffold is proved against a menu with a confirm and a way to Account. */
@Composable
private fun accountRows(
    selectedKey: Any,
    onSelect: (Any) -> Unit,
    contentFocusRequester: FocusRequester,
    onSignOut: () -> Unit,
): List<TvAccountMenuRow> =
    remember(selectedKey, onSelect, onSignOut, contentFocusRequester) {
        listOf(
            TvAccountMenuRow(
                key = "my-lists",
                label = MY_LISTS,
                icon = Icons.Filled.Person,
                onClick = {
                    if (selectedKey == KEY_ACCOUNT) runCatching { contentFocusRequester.requestFocus() } else onSelect(KEY_ACCOUNT)
                },
            ),
            TvAccountMenuRow(
                key = "sign-out",
                label = SIGN_OUT,
                icon = Icons.Filled.Person,
                onClick = {
                    onSignOut()
                    onSelect(KEY_ACCOUNT)
                },
                confirm = TvAccountMenuConfirm(
                    title = SIGN_OUT_TITLE,
                    message = "You'll need to sign in again to sync your watchlist, favourites, and ratings.",
                    confirmLabel = SIGN_OUT,
                ),
            ),
        )
    }
