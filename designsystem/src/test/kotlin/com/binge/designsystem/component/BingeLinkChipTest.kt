package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeLinkChipTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a link chip says it opens outside the app and opens on a tap`() {
        var opened = 0
        rule.setContent { BingeLinkChip(label = "IMDb", leadingIcon = Icons.Filled.Movie, onClick = { opened++ }) }

        rule.onNodeWithContentDescription("IMDb, opens outside the app").assertHasClickAction().performClick()

        assertEquals(1, opened)
    }
}
