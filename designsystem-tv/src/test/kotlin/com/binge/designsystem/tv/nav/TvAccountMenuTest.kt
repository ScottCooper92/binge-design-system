package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardAndroidComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The account menu's rules: it opens while the avatar is focused or focus is inside it; Down enters it, Up leaves it; Back
 * closes only the menu, onto the avatar, and the menu stays closed until focus has left and come back; a destructive row asks
 * first, under an open menu.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvAccountMenuTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardAndroidComposeRule<ComponentActivity>()

    private val shellBack = mutableIntStateOf(0)
    private var switched = 0
    private var signedOut = 0

    private val openChanges = mutableListOf<Boolean>()

    private fun setMenu(
        name: String = "Alexander Hamilton",
        avatarRequester: FocusRequester? = null,
        reportChanges: Boolean = false,
        selected: Boolean = false,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                // The shell's own handler, registered before the menu's, so the menu's wins while it is open.
                BackHandler { shellBack.intValue++ }
                val content = remember { FocusRequester() }
                Column(verticalArrangement = Arrangement.spacedBy(48.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(400.dp)) {
                        Box(Modifier.size(48.dp).semantics { contentDescription = "Previous" }.focusable())
                        TvAccountMenu(
                            avatarLabel = "Account",
                            name = name,
                            detail = "Administrator",
                            down = content,
                            avatarFocusRequester = avatarRequester,
                            selected = selected,
                            onOpenChange = { if (reportChanges) openChanges += it },
                            rows = listOf(
                                TvAccountMenuRow("switch", "Switch profile", Icons.Filled.Settings, { switched++ }),
                                TvAccountMenuRow(
                                    key = "signout",
                                    label = "Sign out",
                                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                                    onClick = { signedOut++ },
                                    confirm = TvAccountMenuConfirm("Sign out?", "You will need to sign in again.", "Confirm sign out"),
                                ),
                            ),
                        )
                    }
                    Box(
                        Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Page" }
                            .focusRequester(content)
                            .focusable(),
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun key(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun pressBack() {
        composeTestRule.runOnIdle { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()
    }

    private fun focusAvatar() {
        composeTestRule.onNodeWithContentDescription("Account").requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun menuIsOpen() = composeTestRule.onAllNodes(hasText("Switch profile")).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `the menu is open while the avatar has focus and closed once focus is elsewhere`() {
        setMenu()
        assertTrue(!menuIsOpen())
        focusAvatar()
        assertTrue(menuIsOpen())
        composeTestRule.onNodeWithContentDescription("Page").requestFocus()
        composeTestRule.waitForIdle()
        assertTrue(!menuIsOpen())
    }

    @Test
    fun `Down from the avatar enters the first row, and Up from it returns to the avatar`() {
        setMenu()
        focusAvatar()
        key(Key.DirectionDown)
        composeTestRule.onNodeWithText("Switch profile").assertIsFocused()
        assertTrue(menuIsOpen())
        key(Key.DirectionUp)
        composeTestRule.onNodeWithContentDescription("Account").assertIsFocused()
        assertTrue(menuIsOpen())
    }

    @Test
    fun `Down goes to the content below while the menu is closed`() {
        setMenu()
        focusAvatar()
        // Held closed by Back: the avatar keeps focus but the menu is not there to enter.
        pressBack()
        assertTrue(!menuIsOpen())
        key(Key.DirectionDown)
        composeTestRule.onNodeWithContentDescription("Page").assertIsFocused()
    }

    @Test
    fun `Back with the avatar focused closes only the menu, and a second Back reaches the shell`() {
        setMenu()
        focusAvatar()
        pressBack()
        assertTrue(!menuIsOpen())
        composeTestRule.onNodeWithContentDescription("Account").assertIsFocused()
        assertEquals("The shell saw the first Back", 0, shellBack.intValue)
        pressBack()
        assertEquals(1, shellBack.intValue)
    }

    @Test
    fun `Back from inside the menu closes it onto the avatar without reaching the shell`() {
        setMenu()
        focusAvatar()
        key(Key.DirectionDown)
        composeTestRule.onNodeWithText("Switch profile").assertIsFocused()
        pressBack()
        assertTrue(!menuIsOpen())
        composeTestRule.onNodeWithContentDescription("Account").assertIsFocused()
        assertEquals(0, shellBack.intValue)
    }

    @Test
    fun `a closed menu stays closed while the avatar keeps focus and reopens after focus leaves and returns`() {
        setMenu()
        focusAvatar()
        pressBack()
        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.waitForIdle()
        assertTrue("Reopened under a focused avatar", !menuIsOpen())
        composeTestRule.onNodeWithContentDescription("Page").requestFocus()
        composeTestRule.waitForIdle()
        focusAvatar()
        assertTrue(menuIsOpen())
    }

    @Test
    fun `Sign out asks first with the menu open under it, and dismissing returns focus to the row`() {
        setMenu()
        focusAvatar()
        key(Key.DirectionDown)
        key(Key.DirectionDown)
        composeTestRule.onNodeWithText("Sign out").assertIsFocused()
        key(Key.DirectionCenter)
        composeTestRule.onNodeWithText("Sign out?").assertExists()
        assertTrue("The menu closed under its dialog", menuIsOpen())
        assertEquals(0, signedOut)
        composeTestRule.onNodeWithText("Cancel").performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sign out").assertIsFocused()
        assertEquals(0, signedOut)
    }

    @Test
    fun `confirming Sign out runs it and leaves focus on the avatar with the menu held closed`() {
        setMenu()
        focusAvatar()
        key(Key.DirectionDown)
        key(Key.DirectionDown)
        key(Key.DirectionCenter)
        // The dialog opens with focus on Cancel, so a stray OK does nothing; move to the confirm answer.
        composeTestRule.onNodeWithText("Confirm sign out").requestFocus()
        composeTestRule.onNodeWithText("Confirm sign out").performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()
        assertEquals(1, signedOut)
        composeTestRule.onNodeWithContentDescription("Account").assertIsFocused()
        assertTrue("The menu reopened over the signed-out header", !menuIsOpen())
    }

    @Test
    fun `leaving sideways closes the menu`() {
        setMenu()
        focusAvatar()
        key(Key.DirectionDown)
        key(Key.DirectionLeft)
        assertTrue("The menu stayed open after focus left it", !menuIsOpen())
        composeTestRule.onNodeWithContentDescription("Account").assertIsNotFocused()
    }

    @Test
    fun `a supplied requester focuses the avatar`() {
        val requester = FocusRequester()
        setMenu(avatarRequester = requester)
        composeTestRule.onNodeWithContentDescription("Account").assertIsNotFocused()
        composeTestRule.runOnIdle { requester.requestFocus() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Account").assertIsFocused()
        assertTrue("The avatar did not open the menu", menuIsOpen())
        key(Key.DirectionDown)
        composeTestRule.onNodeWithText("Switch profile").assertIsFocused()
    }

    @Test
    fun `onOpenChange is true on open, stays true under the confirm, and is false on close`() {
        setMenu(reportChanges = true)
        assertEquals("Nothing is reported before the menu changes", emptyList<Boolean>(), openChanges)
        focusAvatar()
        assertEquals(listOf(true), openChanges)
        key(Key.DirectionDown)
        key(Key.DirectionDown)
        key(Key.DirectionCenter)
        composeTestRule.onNodeWithText("Sign out?").assertExists()
        assertEquals("The confirm dialog counts as open", listOf(true), openChanges)
        composeTestRule.onNodeWithText("Cancel").performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()
        assertEquals(listOf(true), openChanges)
        composeTestRule.onNodeWithContentDescription("Page").requestFocus()
        composeTestRule.waitForIdle()
        assertEquals(listOf(true, false), openChanges)
    }

    @Test
    fun `onOpenChange is false when Back closes the menu`() {
        setMenu(reportChanges = true)
        focusAvatar()
        pressBack()
        assertEquals(listOf(true, false), openChanges)
    }

    @Test
    fun `a selected avatar reads as selected`() {
        setMenu(selected = true)
        composeTestRule.onNodeWithContentDescription("Account").assertIsSelected()
    }

    @Test
    fun `an avatar that is not selected says nothing about selection, as it did before the parameter`() {
        setMenu()
        composeTestRule
            .onNodeWithContentDescription("Account")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }

    @Test
    fun `a selected avatar still opens the menu and keeps its selection while it is open`() {
        setMenu(selected = true)
        focusAvatar()
        assertTrue(menuIsOpen())
        composeTestRule.onNodeWithContentDescription("Account").assertIsSelected()
    }
}
