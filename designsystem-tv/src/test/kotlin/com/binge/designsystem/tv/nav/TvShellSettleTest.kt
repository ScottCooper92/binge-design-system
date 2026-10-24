package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val SEARCH = "search"

/** A shell's settled destination: where it starts, that [TvShellSettle.jumpTo] moves it at once, and that recreation keeps it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TvShellSettleTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `it starts on the key it is given`() {
        lateinit var settle: TvShellSettle
        rule.setContent { settle = rememberTvShellSettle(HOME) }

        assertEquals(HOME, settle.settledKey)
    }

    @Test
    fun `jumpTo settles on a key at once`() {
        lateinit var settle: TvShellSettle
        rule.setContent { settle = rememberTvShellSettle(HOME) }

        settle.jumpTo(SEARCH)

        assertEquals(SEARCH, settle.settledKey)
    }

    @Test
    fun `the settled key survives recreation`() {
        val restoration = StateRestorationTester(rule)
        lateinit var settle: TvShellSettle
        restoration.setContent { settle = rememberTvShellSettle(HOME) }
        settle.jumpTo(SEARCH)

        restoration.emulateSavedInstanceStateRestore()

        assertEquals(SEARCH, settle.settledKey)
    }
}
