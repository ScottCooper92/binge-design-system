package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvCompactButton
import com.binge.designsystem.tv.component.TvCompactIconButton
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** The compact button's two styles at rest and focused, the inert disabled primary, and its icon-only companion. */
@Composable
fun TvCompactButtonSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvCompactButton(label = "Check again", onClick = {}, style = TvButtonStyle.Primary)
            TvCompactButton(label = "Check again", onClick = {}, style = TvButtonStyle.Primary, initiallyFocused = true)
            TvCompactButton(label = "Checking...", onClick = {}, style = TvButtonStyle.Primary, enabled = false)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvCompactButton(label = "Get a new code", onClick = {})
            TvCompactButton(label = "Get a new code", onClick = {}, initiallyFocused = true)
            TvCompactIconButton(icon = Icons.Filled.Add, label = "Add to list", onClick = {})
            TvCompactIconButton(icon = Icons.Filled.Favorite, label = "Favourite", onClick = {}, initiallyFocused = true)
        }
    }
}

/** The compact button while its action runs: a spinner in place of the label at the same width, at rest and focused. */
@Composable
fun TvCompactButtonLoadingSample() {
    Row(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvCompactButton(label = "Try again", onClick = {}, style = TvButtonStyle.Primary)
        TvCompactButton(label = "Try again", onClick = {}, style = TvButtonStyle.Primary, loading = true)
        TvCompactButton(label = "Try again", onClick = {}, style = TvButtonStyle.Primary, loading = true, initiallyFocused = true)
    }
}

/** A message plate whose action is the compact button, the v2 pairing; the plate takes any button in its actions slot. */
@Composable
fun TvMessagePlateCompactButtonSample() {
    TvMessagePlate(
        headline = "No results for \"kurosawa\"",
        body = "Check the spelling, or try a shorter search.",
        icon = Icons.Filled.SearchOff,
        alignment = Alignment.Center,
        actions = { TvCompactButton(label = "Clear search", onClick = {}, style = TvButtonStyle.Primary) },
    )
}
