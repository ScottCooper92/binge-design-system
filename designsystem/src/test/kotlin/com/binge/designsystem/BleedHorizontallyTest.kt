package com.binge.designsystem

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** That [bleedHorizontally] lays its content out past the column's edges while the column itself keeps its width. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BleedHorizontallyTest {
    @get:Rule
    val compose = createTakeDownComposeRule()

    private fun show(by: Dp) =
        compose.setContent {
            Column(Modifier.width(COLUMN).testTag(PARENT)) {
                Box(
                    Modifier
                        .bleedHorizontally(by)
                        .fillMaxWidth()
                        .height(ROW_HEIGHT)
                        .testTag(ROW),
                )
            }
        }

    @Test
    fun `the content runs out by the bleed on each side`() {
        show(BLEED)

        val row = compose.onNodeWithTag(ROW).getUnclippedBoundsInRoot()
        assertEquals(-BLEED, row.left)
        assertEquals(COLUMN + BLEED * 2, row.width)
    }

    @Test
    fun `the column around it keeps its width`() {
        show(BLEED)

        assertEquals(COLUMN, compose.onNodeWithTag(PARENT).getUnclippedBoundsInRoot().width)
    }

    @Test
    fun `no bleed lays the content out as before`() {
        show(0.dp)

        val row = compose.onNodeWithTag(ROW).getUnclippedBoundsInRoot()
        assertEquals(0.dp, row.left)
        assertEquals(COLUMN, row.width)
    }

    @Test
    fun `an unbounded width lays the content out unchanged`() {
        compose.setContent {
            Row(Modifier.width(COLUMN).horizontalScroll(rememberScrollState())) {
                Box(
                    Modifier
                        .bleedHorizontally(BLEED)
                        .width(ROW_WIDTH)
                        .height(ROW_HEIGHT)
                        .testTag(ROW),
                )
            }
        }

        val row = compose.onNodeWithTag(ROW).getUnclippedBoundsInRoot()
        assertEquals(0.dp, row.left)
        assertEquals(ROW_WIDTH, row.width)
    }

    private companion object {
        val COLUMN = 300.dp
        val BLEED = 24.dp
        val ROW_HEIGHT = 48.dp
        val ROW_WIDTH = 500.dp
        const val PARENT = "parent"
        const val ROW = "row"
    }
}
