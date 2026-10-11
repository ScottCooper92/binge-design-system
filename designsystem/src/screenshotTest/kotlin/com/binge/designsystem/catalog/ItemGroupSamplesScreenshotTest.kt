package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [ItemGroup] catalog samples. */
class ItemGroupSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun titled() {
        ItemGroupTitledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tinted() {
        ItemGroupTintedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun badges() {
        ItemGroupBadgesSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun selected() {
        ItemGroupSelectedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun external() {
        ItemGroupExternalSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun action() {
        ItemGroupActionSample()
    }

    /** [ListItem.loading] on the acting row and [ListItem.disabled] on the one it blocks. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun busy() {
        ItemGroupBusySample()
    }

    /** The `rowVerticalPadding` override beside [titled]'s default rows (#133). */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tallRows() {
        ItemGroupTallRowsSample()
    }

    /** The `titleSpacing` override beside [titled]'s default rows. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tightTitle() {
        ItemGroupTightTitleSample()
    }

    /** [ListItem.leadingContent] standing in for the icon box. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun leadingContent() {
        ItemGroupLeadingContentSample()
    }

    /** [ItemRows] with no surface of its own, drawn inside a card the caller supplies. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun noSurface() {
        ItemRowsNoSurfaceSample()
    }

    /** [ListItem.connector] joining three child rows to their parent. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun connector() {
        ItemGroupConnectorSample()
    }

    /** [ListItem.toggled] switch rows: on, off and disabled. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun switches() {
        ItemGroupSwitchSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun mixedSwitches() {
        ItemGroupMixedSwitchSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun longLabels() {
        ItemGroupLongLabelSample()
    }

    /** [ListItem.expanded] rows: one open, one closed. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun expanded() {
        ItemGroupExpandedSample()
    }

    /** [ItemGroup]'s `belowRows` slot on the same surface as the rows. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun BelowRows() {
        ItemGroupBelowRowsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun UntitledNonClickable() {
        ItemGroupUntitledNonClickableSample()
    }

    /** [ListItem.iconPainter] drawn in place of [ListItem.icon]. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun PainterIcon() {
        ItemGroupPainterIconSample()
    }
}
