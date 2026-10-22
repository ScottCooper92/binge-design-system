package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetRows
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.theme.LocalTvStyle
import com.binge.designsystem.tv.theme.TvStyle

/*
 * The components whose look the TV style sets, drawn in TvStyle.Monochrome: white focus with dark content on it, black and
 * white switches and a surfaceVariant panel. Each wraps the component's own sample, so the pair compares state for state
 * with the TvStyle.Accent frame of the same sample.
 */

/** A sheet in the monochrome style: the focused row fills white, while the tick stays in the row's content colour. */
@Composable
fun TvMonochromeSideSheetSample() {
    Monochrome { TvSideSheetSample() }
}

/** A focused destructive row in the monochrome style: it still fills with the error colour, not white. */
@Composable
fun TvMonochromeSideSheetDestructiveSample() {
    Monochrome {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            TvSideSheetPanel {
                TvSideSheetTitle("Dune: Part Two")
                TvSideSheetRows {
                    TvSideSheetRow(label = "Approve", onClick = {}, icon = Icons.Filled.Check)
                    TvSideSheetRow(label = "4K", onClick = {}, selected = true)
                    TvSideSheetRow(
                        label = "Delete request",
                        onClick = {},
                        icon = Icons.Filled.Delete,
                        destructive = true,
                        initiallyFocused = true,
                    )
                }
            }
        }
    }
}

/** The confirm step in the monochrome style: Cancel focused in white, the confirm row resting in the error colour. */
@Composable
fun TvMonochromeSideSheetConfirmSample() {
    Monochrome { TvSideSheetConfirmSample() }
}

/** Switches in the monochrome style: the focused one on, dark against the white fill, and the resting ones white. */
@Composable
fun TvMonochromeSwitchSample() {
    Monochrome { TvSwitchSample() }
}

/** The monochrome sheet with its focused switch off: a dark outline and thumb on the white fill. */
@Composable
fun TvMonochromeSwitchOffSample() {
    Monochrome { TvSwitchOffSample() }
}

/** A checked destructive switch in the monochrome style, focused and not: red in both, as in the accent style. */
@Composable
fun TvMonochromeSwitchDestructiveSample() {
    Monochrome { TvSwitchDestructiveCheckedSample() }
}

/** The buttons in the monochrome style: focus fills white, and the red styles keep the error colour. */
@Composable
fun TvMonochromeButtonSample() {
    Monochrome { TvButtonSample() }
}

/** The confirm dialog in the monochrome style: a surfaceVariant panel, with Cancel focused in white. */
@Composable
fun TvMonochromeConfirmDialogSample() {
    Monochrome { TvConfirmDialogSample() }
}

/** A pill choice row in the monochrome style: focus fills white, and the chosen pill keeps its tick. */
@Composable
fun TvMonochromeChoiceRowSample() {
    Monochrome { TvChoiceRowSample() }
}

/** An option group in the monochrome style: focus fills white, and the chosen option keeps the accent label and tick. */
@Composable
fun TvMonochromeOptionGroupSample() {
    Monochrome { TvOptionGroupSample() }
}

/** Checkbox rows in the monochrome style: the focused row fills white, its box and tick dark on it. */
@Composable
fun TvMonochromeCheckboxRowSample() {
    Monochrome { TvCheckboxRowSample() }
}

/** The focus ring in the monochrome style: white, in the same place and width as the accent ring. */
@Composable
fun TvMonochromeFocusIndicatorSample() {
    Monochrome { TvFocusIndicatorSample() }
}

/** A focused tab row in the monochrome style: the focused tab fills white. */
@Composable
fun TvMonochromeTabRowFocusedSample() {
    Monochrome { TvTabRowFocusedSample() }
}

/** Text fields in the monochrome style: the focused field's ring is white. */
@Composable
fun TvMonochromeTextFieldSample() {
    Monochrome { TvTextFieldStatesSample() }
}

@Composable
private fun Monochrome(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTvStyle provides TvStyle.Monochrome, content = content)
}
