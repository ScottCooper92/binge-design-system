package com.binge.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * The variants the catalog sample does not show: a non-clickable row (no chevron), a row drawn
 * from its painter slot, and a badge with no [ListItem.badgeTint] of its own.
 */
class ItemGroupScreenshotTest {
    /** [belowRows] sits on the same surface as the rows, not on a card of its own. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun BelowRows() {
        ScreenshotTheme {
            ItemGroup(
                title = null,
                rows = listOf(ListItem(icon = Icons.Filled.Star, label = "Parent", detail = "Expanded")),
                belowRows = {
                    HorizontalDivider()
                    Text(text = "Expanded content", modifier = Modifier.padding(16.dp))
                },
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun UntitledNonClickable() {
        ScreenshotTheme {
            ItemGroup(
                title = null,
                rows = listOf(
                    ListItem(
                        icon = Icons.Filled.Notifications,
                        label = "Notifications",
                        detail = "Push alerts enabled",
                        clickable = false,
                    ),
                ),
            )
        }
    }

    /** [ListItem.badgeTint] left null: the count renders as the default Material badge, not a tinted pill. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun DefaultBadge() {
        ScreenshotTheme {
            ItemGroup(
                title = null,
                rows = listOf(
                    ListItem(
                        icon = Icons.Filled.Notifications,
                        label = "Notifications",
                        detail = "Push alerts enabled",
                        badgeCount = 4,
                    ),
                ),
            )
        }
    }

    /** The painter slot wins over [ListItem.icon]: the row draws the star, never the bell it names as fallback. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun PainterIcon() {
        ScreenshotTheme {
            ItemGroup(
                title = null,
                rows = listOf(
                    ListItem(
                        icon = Icons.Filled.Notifications,
                        iconPainter = { rememberVectorPainter(Icons.Filled.Star) },
                        label = "Seerr",
                        detail = "A glyph resolved by the caller",
                    ),
                ),
            )
        }
    }
}
