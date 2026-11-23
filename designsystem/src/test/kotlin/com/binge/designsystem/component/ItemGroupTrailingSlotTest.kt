package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A trailing slot whose count text is weighted (`weight(fill = false)`) keeps its chevron and a readable count at every font scale:
 * the chevron is measured first, and the count gives way with an ellipsis.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ItemGroupTrailingSlotTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun check(fontScale: Float) {
        rule.setContent { Fixture(fontScale) }
        rule.waitForIdle()
        val chevron = rule.onNodeWithTag(CHEVRON, useUnmergedTree = true).fetchSemanticsNode()
        val count = rule.onNodeWithTag(COUNT, useUnmergedTree = true).fetchSemanticsNode()
        assertEquals("the chevron keeps its width at font $fontScale", (CHEVRON_DP * rule.density.density).toInt(), chevron.size.width)
        // The count text keeps at least its longest word ("requests" or "12,840"), not a sliver.
        assertTrue("the count is ${count.size.width}px wide at font $fontScale", count.size.width >= MIN_COUNT_PX)
        rule.assertNoTextClipped()
    }

    /** A long hyphenated name beside a chevron: the label may wrap at its hyphen but is never broken inside a word. At 2x the hyphenated word is wider than the row, so it cannot be kept whole and is not asserted. */
    private fun checkHyphenatedLabel(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    Box(Modifier.width(360.dp)) {
                        ItemGroup(
                            title = null,
                            rows =
                                listOf(
                                    ListItem(
                                        icon = Icons.Filled.Person,
                                        label = "Alexandria Fitzgerald-Montgomery",
                                        trailingContent = { Box(Modifier.size(CHEVRON_DP.dp)) },
                                    ),
                                ),
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
        rule.assertNoTextClipped()
    }

    @Composable
    private fun Fixture(fontScale: Float) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
            TestTheme {
                Box(Modifier.width(360.dp)) {
                    ItemGroup(
                        title = null,
                        rows =
                            listOf(
                                ListItem(
                                    icon = Icons.Filled.Person,
                                    label = "Alexandria Montgomery",
                                    detail = "alexandria.montgomery · Plex",
                                    trailingContent = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                "12,840 requests",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false).testTag(COUNT),
                                            )
                                            Box(Modifier.size(CHEVRON_DP.dp).testTag(CHEVRON))
                                        }
                                    },
                                ),
                            ),
                    )
                }
            }
        }
    }

    @Test fun `the chevron and count survive at 1x`() = check(1f)

    @Test fun `the chevron and count survive at 1_3`() = check(1.3f)

    @Test fun `the chevron and count survive at 2x`() = check(2f)

    @Test fun `a long hyphenated label is not broken at 1x`() = checkHyphenatedLabel(1f)

    @Test fun `a long hyphenated label is not broken at 1_3`() = checkHyphenatedLabel(1.3f)

    private companion object {
        const val CHEVRON = "chevron"
        const val COUNT = "count"
        const val CHEVRON_DP = 24
        const val MIN_COUNT_PX = 40
    }
}
