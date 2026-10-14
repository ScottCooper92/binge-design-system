package com.binge.designsystem.component

import android.content.res.Configuration
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val ROOT_TEXT = "Root page"
private const val DETAIL_TEXT = "Detail page"
private const val OPEN = "Open"

/** Near the start edge, well clear of the panel, which sits at the end edge. */
private const val SCRIM_TOUCH_FRACTION = 0.1f

/**
 * One sheet, two forms: what a task sheet does on a wide window, where pages stack inside it, and what it keeps doing on a
 * narrow one, where a menu always stays.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h900dp")
class BingeAdaptiveSheetExpandedTest {
    @get:Rule
    val rule = createComposeRule()

    private var dismissals = 0
    private val leftPages = mutableListOf<String>()
    private var back: OnBackPressedDispatcherOwner? = null
    private var stacks: Boolean? = null
    private var navHandle: BingeSheetNav? = null

    private fun show(kind: BingeSheetKind = BingeSheetKind.Task, gesturesEnabled: Boolean = true) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeAdaptiveSheet(
                    kind = kind,
                    onDismissRequest = { dismissals++ },
                    onPageBack = { leftPages += it },
                    gesturesEnabled = gesturesEnabled,
                ) { nav -> Pages(nav) }
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun ColumnScope.Pages(nav: BingeSheetNav) {
        back = LocalOnBackPressedDispatcherOwner.current
        stacks = nav.stacksPages
        navHandle = nav
        if (nav.page == "detail") {
            Text(DETAIL_TEXT)
        } else {
            Text(ROOT_TEXT)
            Text(OPEN, modifier = Modifier.clickable { nav.push("detail") })
        }
    }

    private fun pressBack() {
        rule.runOnIdle { checkNotNull(back).onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun backBar() = rule.onAllNodes(hasContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_navigate_back)))

    @Test
    fun `a task sheet is a side sheet that stacks pages`() {
        show()

        assertEquals(true, stacks)
        rule.onNodeWithText(ROOT_TEXT).assertExists()
    }

    @Test
    fun `a pushed page shows under a Back bar and the root is not shown`() {
        show()

        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        rule.onNodeWithText(DETAIL_TEXT).assertExists()
        rule.onNodeWithText(ROOT_TEXT).assertDoesNotExist()
        backBar().assertCountEquals(1)
    }

    @Test
    fun `the root shows no Back bar`() {
        show()

        backBar().assertCountEquals(0)
    }

    @Test
    fun `system Back goes back a page, and from the root closes the sheet`() {
        show()
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        pressBack()

        rule.onNodeWithText(ROOT_TEXT).assertExists()
        assertEquals(0, dismissals)

        pressBack()

        assertEquals(1, dismissals)
    }

    @Test
    fun `a page left by Back is reported, and closing from the root is not a page`() {
        show()
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        pressBack()
        assertEquals(listOf("detail"), leftPages)

        pressBack()
        assertEquals(listOf("detail"), leftPages)
        assertEquals(1, dismissals)
    }

    @Test
    fun `a locked sheet ignores Back and its scrim, so a write in flight is not walked away from`() {
        show(gesturesEnabled = false)
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        pressBack()
        backBar().onFirst().performClick()
        rule
            .onNode(hasContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_close_sheet)) and hasClickAction())
            .performTouchInput { click(Offset(width * SCRIM_TOUCH_FRACTION, centerY)) }
        rule.waitForIdle()

        rule.onNodeWithText(DETAIL_TEXT).assertExists()
        assertEquals(emptyList<String>(), leftPages)
        assertEquals(0, dismissals)
    }

    @Test
    fun `nav pop goes back a page and reports it, then closes from the root`() {
        show()
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        rule.runOnIdle { checkNotNull(navHandle).pop() }
        rule.waitForIdle()
        assertEquals(listOf("detail"), leftPages)
        rule.onNodeWithText(ROOT_TEXT).assertExists()
        assertEquals(0, dismissals)

        rule.runOnIdle { checkNotNull(navHandle).pop() }
        assertEquals(1, dismissals)
        assertEquals(listOf("detail"), leftPages)
    }

    @Test
    fun `nav pop is not held by the lock, which stops only the user's Back`() {
        show(gesturesEnabled = false)
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        rule.runOnIdle { checkNotNull(navHandle).pop() }
        rule.waitForIdle()

        assertEquals(listOf("detail"), leftPages)
        rule.onNodeWithText(ROOT_TEXT).assertExists()
    }

    @Test
    fun `the Back bar goes back a page`() {
        show()
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        backBar().onFirst().performClick()
        rule.waitForIdle()

        rule.onNodeWithText(ROOT_TEXT).assertExists()
        assertEquals(0, dismissals)
    }

    @Test
    fun `a touch on the scrim closes the whole sheet from a stacked page`() {
        show()
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        rule
            .onNode(hasContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_close_sheet)) and hasClickAction())
            .performTouchInput { click(Offset(width * SCRIM_TOUCH_FRACTION, centerY)) }
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    @Test
    fun `a menu stays a bottom sheet that does not stack`() {
        show(BingeSheetKind.Menu)

        assertEquals(false, stacks)
        // The side sheet's scrim is the only node named for closing it; a bottom sheet has none.
        rule.onAllNodes(hasContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_close_sheet))).assertCountEquals(0)
    }

    @Test
    fun `push does nothing in a menu`() {
        show(BingeSheetKind.Menu)

        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        rule.onNodeWithText(ROOT_TEXT).assertExists()
        rule.onNodeWithText(DETAIL_TEXT).assertDoesNotExist()
    }
}

/** Below the expanded width a task sheet is the bottom sheet it always was, and pages do not stack. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
class BingeAdaptiveSheetCompactTest {
    @get:Rule
    val rule = createComposeRule()

    private var stacks: Boolean? = null

    @Test
    fun `a task sheet below the expanded width is a bottom sheet and does not stack`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}) { nav ->
                    stacks = nav.stacksPages
                    Text(ROOT_TEXT)
                    Text(OPEN, modifier = Modifier.clickable { nav.push("detail") })
                }
            }
        }
        rule.waitForIdle()

        assertEquals(false, stacks)
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(ROOT_TEXT).assertExists()
        rule
            .onAllNodes(
                hasContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_navigate_back)),
            ).assertCountEquals(0)
    }
}

/** A window that crosses the expanded width mid-session keeps the page the sheet was on. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h900dp")
class BingeAdaptiveSheetResizeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a pushed page survives the window narrowing and widening`() {
        var wide by mutableStateOf(true)
        var stacks: Boolean? = null
        val leftPages = mutableListOf<String>()
        rule.setContent {
            val base = LocalContext.current
            val baseConfiguration = LocalConfiguration.current
            val context =
                remember(wide, base, baseConfiguration) {
                    val config = Configuration(baseConfiguration)
                    config.screenWidthDp = if (wide) WIDE_DP else NARROW_DP
                    base.createConfigurationContext(config)
                }
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides context.resources.configuration) {
                BingeExpressiveTheme(dynamicColor = false) {
                    BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = {}, onPageBack = { leftPages += it }) { nav ->
                        stacks = nav.stacksPages
                        if (nav.page == "detail") {
                            Text(DETAIL_TEXT)
                        } else {
                            Text(ROOT_TEXT)
                            Text(OPEN, modifier = Modifier.clickable { nav.push("detail") })
                        }
                    }
                }
            }
        }
        rule.onNodeWithText(OPEN).performClick()
        rule.waitForIdle()

        wide = false
        rule.waitForIdle()

        assertEquals(false, stacks)
        rule.onNodeWithText(DETAIL_TEXT).assertExists()

        wide = true
        rule.waitForIdle()

        assertEquals(true, stacks)
        rule.onNodeWithText(DETAIL_TEXT).assertExists()
        assertEquals(emptyList<String>(), leftPages)
    }
}

private const val WIDE_DP = 900
private const val NARROW_DP = 412
