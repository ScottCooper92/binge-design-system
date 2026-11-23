package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the floating composer. Each renders the shared `…Sample()`, the component's one public fixture.
 * The text-bearing frames also run at 1.3x and 2x font, where the row of actions and the field are what could clip.
 */
class BingeFloatingComposerSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Empty() {
        BingeFloatingComposerSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Typed() {
        BingeFloatingComposerTypedSample()
    }

    /** Past five lines the field scrolls instead of growing the card. */
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongText() {
        BingeFloatingComposerLongTextSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Sending() {
        BingeFloatingComposerSendingSample()
    }

    /** The card above the keyboard, which the frame gives it as a bottom inset. */
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun KeyboardOpen() {
        BingeFloatingComposerKeyboardOpenSample()
    }

    /** Keyboard closed under a navigation bar: the card clears it. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun UnderSystemBars() {
        PreviewSystemBarInsets { BingeFloatingComposerTypedSample() }
    }
}
