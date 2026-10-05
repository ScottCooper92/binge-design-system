package com.binge.designsystem.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BingeSheetDockingTest {
    private val statusBarBottom = 100f
    private val travel = 80f

    @Test
    fun `a sheet more than the travel below the status bar is not docked at all`() {
        assertEquals(0f, dockFraction(sheetTop = 180f, statusBarBottom = statusBarBottom, travel = travel))
        assertEquals(0f, dockFraction(sheetTop = 900f, statusBarBottom = statusBarBottom, travel = travel))
    }

    @Test
    fun `the fraction rises linearly over the travel and holds at one under the status bar`() {
        assertEquals(0.5f, dockFraction(sheetTop = 140f, statusBarBottom = statusBarBottom, travel = travel))
        assertEquals(1f, dockFraction(sheetTop = 100f, statusBarBottom = statusBarBottom, travel = travel))
        assertEquals(1f, dockFraction(sheetTop = 0f, statusBarBottom = statusBarBottom, travel = travel))
    }

    @Test
    fun `a footer already above the navigation bar is not lifted`() {
        assertEquals(0f, pinnedFooterLift(naturalBottom = 1500f, windowHeight = 2000, navigationBarHeight = 100))
        assertEquals(0f, pinnedFooterLift(naturalBottom = 1900f, windowHeight = 2000, navigationBarHeight = 100))
    }

    @Test
    fun `a footer below the navigation bar's top is lifted to sit on it`() {
        assertEquals(600f, pinnedFooterLift(naturalBottom = 2500f, windowHeight = 2000, navigationBarHeight = 100))
        assertEquals(500f, pinnedFooterLift(naturalBottom = 2500f, windowHeight = 2000, navigationBarHeight = 0))
    }
}
