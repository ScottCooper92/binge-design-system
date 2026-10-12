package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [ListItem.enabled] is the Material 3 name for the flag that was `disabled`, turned round. */
class ListItemEnabledTest {
    private val icon = Icons.Filled.Bookmark

    @Test
    fun `a row is enabled unless it says otherwise`() {
        assertTrue(ListItem(icon = icon, label = "Row").enabled)
        assertFalse(ListItem(icon = icon, label = "Row", enabled = false).enabled)
    }
}
