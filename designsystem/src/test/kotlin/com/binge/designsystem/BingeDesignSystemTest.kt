package com.binge.designsystem

import org.junit.Assert.assertTrue
import org.junit.Test

class BingeDesignSystemTest {
    @Test
    fun `the design system names the consumers it is shared with`() {
        assertTrue(BingeDesignSystem.CONSUMERS.contains("Binge"))
        assertTrue(BingeDesignSystem.CONSUMERS.contains("binge-seerr"))
    }
}
