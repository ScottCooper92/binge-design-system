@file:CatalogGroup("Choices")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvChoiceRow
import com.binge.designsystem.tv.component.TvChoiceUi
import com.binge.designsystem.tv.component.TvOptionRow
import com.binge.designsystem.tv.component.TvRowIcon
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/*
 * Rows that lead with an icon on its tinted plate. Focus is seeded through each row's initial-focus parameter, so the
 * focused plate, which inverts with the fill, is captured without a focus request.
 */

private const val KEY_MOVIES = "movies"
private const val KEY_TV = "tv"

/** The bare plate: the glyph in the accent on a wash of it, at rest, in a custom tint, and on the focus fill. */
@Composable
fun TvRowIconSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        TvRowIcon(icon = Icons.Filled.HighQuality)
        TvRowIcon(icon = Icons.Filled.Delete, tint = MaterialTheme.colorScheme.error)
        TvRowIcon(icon = Icons.Filled.HighQuality, isFocused = true)
    }
}

/** An option row, two checkbox rows (one ticked and focused) and a pill choice row, each leading with an icon. */
@Composable
fun TvRowIconRowsSample() {
    var quality by remember { mutableStateOf(true) }
    var subtitles by remember { mutableStateOf(true) }
    var captions by remember { mutableStateOf(false) }
    var kind by remember { mutableStateOf(KEY_MOVIES) }
    Column(
        modifier =
            Modifier
                .padding(dimensionResource(DesR.dimen.padding_l))
                .width(dimensionResource(TvR.dimen.tv_list_pane_option_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        TvOptionRow(label = "1080p", selected = quality, onClick = { quality = true }, icon = Icons.Filled.HighQuality)
        TvCheckboxRow(
            label = "Subtitles",
            checked = subtitles,
            onCheckedChange = { subtitles = it },
            icon = Icons.Filled.Subtitles,
            initiallyFocused = true,
        )
        TvCheckboxRow(label = "Captions", checked = captions, onCheckedChange = { captions = it }, icon = Icons.Filled.Subtitles)
        TvChoiceRow(
            choices =
                listOf(
                    TvChoiceUi(KEY_MOVIES, "Films", Icons.Filled.Movie),
                    TvChoiceUi(KEY_TV, "Series", Icons.Filled.Tv),
                ),
            selectedKey = kind,
            onSelect = { kind = it },
            initialFocusedKey = KEY_TV,
        )
    }
}

/** The side sheet's rows with an icon: tinted at rest, in the error colour when destructive, dimmed when disabled, and on the focus fill. */
@Composable
fun TvRowIconSideSheetSample() {
    TvSideSheetPanel {
        TvSideSheetRow(label = "Approve", onClick = {}, icon = Icons.Filled.Check, initiallyFocused = true)
        TvSideSheetRow(label = "Subtitles", onClick = {}, icon = Icons.Filled.Subtitles)
        TvSideSheetRow(label = "Delete request", onClick = {}, icon = Icons.Filled.Delete, destructive = true)
        TvSideSheetRow(label = "Already approved", onClick = {}, icon = Icons.Filled.Check, enabled = false)
    }
}
