package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import org.junit.Assert.assertEquals
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

    @Test
    @Suppress("DEPRECATION")
    fun `the old disabled flag builds the same row, turned round`() {
        val onClick = {}
        val old = ListItem(icon = icon, label = "Row", detail = "Detail", clickable = false, onClick = onClick, disabled = true)
        assertEquals(ListItem(icon = icon, label = "Row", detail = "Detail", clickable = false, onClick = onClick, enabled = false), old)
        assertTrue(old.disabled)
        assertFalse(ListItem(icon = icon, label = "Row", disabled = false).disabled)
    }
}
