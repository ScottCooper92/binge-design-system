@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.SeasonDropdown
import com.binge.designsystem.component.SeasonPickerContent
import com.binge.designsystem.component.SeasonPickerSheet
import com.binge.designsystem.component.SeasonRow
import com.binge.designsystem.preview.ScreenshotTheme

private val seasons = listOf(
    SeasonRow(0, "Specials", "3 episodes"),
    SeasonRow(1, "Season 1", "10 episodes · 2011 · Available"),
    SeasonRow(2, "Season 2", "10 episodes · 2012 · Available"),
    SeasonRow(3, "Season 3", "10 episodes · 2013"),
)

/** The chip in a series page's Episodes header: the season shown and a down arrow. Tapping it in the catalog counts a tap. */
@Composable
fun SeasonDropdownSample() {
    ScreenshotTheme {
        var taps by remember { mutableIntStateOf(0) }
        Box { SeasonDropdown(label = if (taps == 0) "Season 2" else "Season 2 · $taps", onClick = { taps++ }) }
    }
}

/** A name longer than the chip's cap: it ends in an ellipsis and the arrow stays in view. */
@Composable
fun SeasonDropdownLongLabelSample() {
    ScreenshotTheme { Box { SeasonDropdown(label = "Season 4: The Long Night and the Dawn After", onClick = {}) } }
}

/** The season picker's rows: each season with its episodes, year and status, the shown one with a check. Tapping a row moves the check. */
@Composable
fun SeasonPickerContentSample() {
    ScreenshotTheme {
        var selected by remember { mutableIntStateOf(2) }
        SeasonPickerContent(rows = seasons, selected = selected, onSelect = { selected = it })
    }
}

private val richSeasons = listOf(
    SeasonRow(0, "Specials", "3 episodes", overview = "Behind-the-scenes shorts and the two holiday episodes."),
    SeasonRow(
        1,
        "Season 1",
        "10 episodes · 2011 · Available",
        overview = "A quiet town's first winter, and the stranger who arrives with the thaw.",
    ),
    SeasonRow(
        2,
        "Season 2",
        "10 episodes · 2012 · Available",
        overview = "Old debts come due, and the town learns what the stranger left behind when the snow melted.",
    ),
    SeasonRow(3, "Season 3", "10 episodes · 2013"),
)

/**
 * The season picker's rows when the seasons carry a synopsis (and, in an app, a poster): the shown season is bold with a check,
 * a long overview ends after two lines.
 */
@Composable
fun SeasonPickerRichContentSample() {
    ScreenshotTheme {
        var selected by remember { mutableIntStateOf(2) }
        SeasonPickerContent(rows = richSeasons, selected = selected, onSelect = { selected = it })
    }
}

/** The season picker opened for real, over a button: it stacks over the page as a task sheet, and choosing a season closes it. */
@Composable
fun SeasonPickerSheetDemo() {
    var open by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(2) }
    ScreenshotTheme {
        Column(Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m))) {
            BingeFilledButton(label = "Season $selected: pick another", onClick = { open = true })
        }
        if (open) {
            SeasonPickerSheet(rows = seasons, selected = selected, onSelect = {
                selected = it
                open = false
            }, onDismiss = { open = false })
        }
    }
}
