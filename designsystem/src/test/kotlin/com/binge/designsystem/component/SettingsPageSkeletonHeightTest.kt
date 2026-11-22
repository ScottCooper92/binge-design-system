package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A settings-page skeleton row is as tall as the [ItemGroup] row it stands in for, at the default font and at 2x: the settled
 * row grows with the font once its text is taller than the icon plate, and the placeholder must grow with it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SettingsPageSkeletonHeightTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun heights(fontScale: Float, detail: Boolean): Pair<Dp, Dp> {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Column {
                        Box(Modifier.testTag("real").fillMaxWidth()) {
                            ItemGroup(
                                title = null,
                                rows = listOf(
                                    ListItem(icon = Icons.Filled.Settings, label = "Label", detail = if (detail) "Detail" else null),
                                ),
                            )
                        }
                        ListRowSkeleton(
                            modifier = Modifier.testTag("skeleton").fillMaxWidth(),
                            height = itemGroupRowHeight(detail),
                        )
                    }
                }
            }
        }
        val real = rule.onNodeWithTag("real").getUnclippedBoundsInRoot().let { it.bottom - it.top }
        val skeleton = rule.onNodeWithTag("skeleton").getUnclippedBoundsInRoot().let { it.bottom - it.top }
        return real to skeleton
    }

    @Test
    fun `a row with a detail line matches at the default font`() {
        val (real, skeleton) = heights(fontScale = 1f, detail = true)
        assertEquals(real.value, skeleton.value, TOLERANCE)
    }

    @Test
    fun `a row with a detail line matches at 2x`() {
        val (real, skeleton) = heights(fontScale = 2f, detail = true)
        assertEquals(real.value, skeleton.value, TOLERANCE)
    }

    @Test
    fun `a label-only row matches at the default font`() {
        val (real, skeleton) = heights(fontScale = 1f, detail = false)
        assertEquals(real.value, skeleton.value, TOLERANCE)
    }

    @Test
    fun `a label-only row matches at 2x`() {
        val (real, skeleton) = heights(fontScale = 2f, detail = false)
        assertEquals(real.value, skeleton.value, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1f
    }
}
