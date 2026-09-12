package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.TextEntrySurface
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Phone-sized height for the expanded mode, which fills its host's bounded height. */
private val EXPANDED_HEIGHT = 720.dp

/**
 * Reusable text-entry surface — title, optional header slot, multi-line field, and a
 * cancel/submit row. Covers the empty (submit disabled), filled, submitting, error,
 * character-count, header-slot, collapsed-with-expand-affordance and expanded/locked
 * full-screen states across the colour axis.
 */
class TextEntrySurfaceScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Empty() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Add a comment",
                value = "",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Comment",
                hint = "Add a comment…",
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Filled() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Add a comment",
                value = "Re-grabbed the file — should be sorted now.",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Comment",
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Submitting() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Edit comment",
                value = "Re-grabbed the file.",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Save",
                isSubmitting = true,
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Error() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Add a comment",
                value = "oops",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Comment",
                error = "Couldn't post your comment. Try again.",
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithCounter() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Edit description",
                value = "Audio drifts out of sync about 20 minutes in.",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Save",
                maxLength = 500,
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongContent() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Add a comment",
                value =
                    "Tried a fresh grab and it still desyncs. The audio is fine for the first few " +
                        "minutes, then it drifts about half a second ahead and keeps sliding from there. " +
                        "Happens on both the web player and the cast target, so it looks like the source " +
                        "file rather than playback. Re-encoding the audio track or re-pulling the release " +
                        "would probably fix it. Let me know if you want a sample clip.",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Comment",
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithTypePicker() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Report an issue",
                value = "",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Report",
                hint = "Describe the problem…",
                header = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BingeFilterChip(label = "Video", selected = true, onClick = {})
                        BingeFilterChip(label = "Audio", selected = false, onClick = {})
                        BingeFilterChip(label = "Other", selected = false, onClick = {})
                    }
                },
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Collapsible() {
        ScreenshotTheme {
            TextEntrySurface(
                title = "Add a comment",
                value = "Re-grabbed the file — should be sorted now.",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Comment",
                expanded = false,
                onExpandToggle = {},
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Expanded() {
        ScreenshotTheme(modifier = Modifier.height(EXPANDED_HEIGHT)) {
            TextEntrySurface(
                title = "Add a comment",
                value =
                    "Tried a fresh grab and it still desyncs. The audio is fine for the first few " +
                        "minutes, then it drifts about half a second ahead and keeps sliding from there. " +
                        "Happens on both the web player and the cast target, so it looks like the source " +
                        "file rather than playback. Re-encoding the audio track or re-pulling the release " +
                        "would probably fix it. Let me know if you want a sample clip.",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = "Comment",
                expanded = true,
                onExpandToggle = {},
            )
        }
    }
}
