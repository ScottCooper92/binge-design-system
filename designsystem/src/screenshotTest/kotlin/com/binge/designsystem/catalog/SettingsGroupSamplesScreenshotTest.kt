package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [SettingsGroup] catalog samples. */
class SettingsGroupSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun titled() {
        SettingsGroupTitledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tinted() {
        SettingsGroupTintedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun badges() {
        SettingsGroupBadgesSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun selected() {
        SettingsGroupSelectedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun external() {
        SettingsGroupExternalSample()
    }

    /** [SettingsRow.loading] on the acting row and [SettingsRow.disabled] on the one it blocks. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun busy() {
        SettingsGroupBusySample()
    }

    /** The `rowVerticalPadding` override beside [titled]'s default rows (#133). */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tallRows() {
        SettingsGroupTallRowsSample()
    }

    /** The `titleSpacing` override beside [titled]'s default rows. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tightTitle() {
        SettingsGroupTightTitleSample()
    }

    /** [SettingsRow.leadingContent] standing in for the icon box. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun leadingContent() {
        SettingsGroupLeadingContentSample()
    }

    /** [SettingsRows] with no surface of its own, drawn inside a card the caller supplies. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun noSurface() {
        SettingsRowsNoSurfaceSample()
    }

    /** [SettingsRow.connector] joining three child rows to their parent. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun connector() {
        SettingsGroupConnectorSample()
    }
}
