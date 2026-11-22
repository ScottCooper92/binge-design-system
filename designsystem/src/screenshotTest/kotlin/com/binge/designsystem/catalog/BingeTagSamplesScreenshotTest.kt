package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the tag catalog samples — renders the shared [BingeTagNeutralSample] and
 * [BingeTagTintedSample].
 */
class BingeTagSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Neutral() {
        BingeTagNeutralSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tinted() {
        BingeTagTintedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tags() {
        BingeTagKindsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WrappingRow() {
        WrappingRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun TagRow() {
        TagRowSample()
    }

    /** A tag with room for its label, and one too narrow for a word, which ellipsizes rather than break it. */
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun NarrowTag() {
        BingeTagNarrowSample()
    }

    /** The keywords block: even 8dp gaps between tags of any length, and the heading's gap to the first row as elsewhere. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Keywords() {
        TagRowKeywordsSample()
    }

    @PreviewTest
    @Preview(name = "font13-light", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.3f)
    @Composable
    fun KeywordsLargeFont() {
        TagRowKeywordsSample()
    }
}
