package com.binge.designsystem.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** When the detail bar hands the status bar icons from the hero to the theme. */
class DetailBarStatusBarIconsTest {
    @Test
    fun `icons stay light over a hero in a light theme`() {
        assertFalse(detailBarWantsDarkStatusBarIcons(progress = 0f, heroUnderBar = true, isDarkTheme = false))
        assertFalse(detailBarWantsDarkStatusBarIcons(progress = 0.49f, heroUnderBar = true, isDarkTheme = false))
    }

    @Test
    fun `icons go dark once the scrim is halfway in a light theme`() {
        assertTrue(detailBarWantsDarkStatusBarIcons(progress = 0.5f, heroUnderBar = true, isDarkTheme = false))
        assertTrue(detailBarWantsDarkStatusBarIcons(progress = 1f, heroUnderBar = true, isDarkTheme = false))
    }

    @Test
    fun `icons follow a light theme at rest when no hero is under the bar`() {
        assertTrue(detailBarWantsDarkStatusBarIcons(progress = 0f, heroUnderBar = false, isDarkTheme = false))
    }

    @Test
    fun `icons stay light at any scroll in a dark theme`() {
        assertFalse(detailBarWantsDarkStatusBarIcons(progress = 0f, heroUnderBar = true, isDarkTheme = true))
        assertFalse(detailBarWantsDarkStatusBarIcons(progress = 1f, heroUnderBar = true, isDarkTheme = true))
        assertFalse(detailBarWantsDarkStatusBarIcons(progress = 0f, heroUnderBar = false, isDarkTheme = true))
    }
}
