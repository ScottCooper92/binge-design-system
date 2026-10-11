package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvButtonSurface
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.component.TvIconButtonSurface
import com.binge.designsystem.tv.theme.TvButtonStyle
import kotlinx.coroutines.delay
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val DEMO_LOADING_MILLIS = 2_000L

/**
 * The button while its action runs: a spinner over the content, at rest and focused, beside the same button idle so
 * the width can be seen not to change.
 */
@Composable
fun TvButtonLoadingSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButtonSurface("Sign in", TvButtonStyle.Primary, enabled = true, isFocused = false)
            TvButtonSurface("Sign in", TvButtonStyle.Primary, enabled = true, isFocused = false, loading = true)
            TvButtonSurface("Sign in", TvButtonStyle.Primary, enabled = true, isFocused = true, loading = true)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButtonSurface("Refresh", TvButtonStyle.Secondary, enabled = true, isFocused = false, leadingIcon = Icons.Filled.Refresh)
            TvButtonSurface(
                "Refresh",
                TvButtonStyle.Secondary,
                enabled = true,
                isFocused = false,
                leadingIcon = Icons.Filled.Refresh,
                loading = true,
            )
            TvButtonSurface("Sign out", TvButtonStyle.Destructive, enabled = true, isFocused = true, loading = true)
        }
    }
}

/** The icon button while its action runs: the spinner takes the icon's place, at rest and in the focused reveal. */
@Composable
fun TvIconButtonLoadingSample() {
    Row(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvIconButtonSurface(Icons.Filled.Add, "Add to list", TvButtonStyle.Secondary, enabled = true, isFocused = false, loading = true)
        TvIconButtonSurface(Icons.Filled.Add, "Add to list", TvButtonStyle.Secondary, enabled = true, isFocused = true, loading = true)
        TvIconButtonSurface(Icons.Filled.Delete, "Remove", TvButtonStyle.Destructive, enabled = true, isFocused = true, loading = true)
    }
}

/**
 * The real buttons: OK starts a two-second action, the spinner turns, and OK again does nothing until it ends. Focus
 * stays on the button the whole time.
 */
@Composable
fun TvButtonLoadingDemo() {
    var buttonLoading by remember { mutableStateOf(false) }
    var iconLoading by remember { mutableStateOf(false) }
    LaunchedEffect(buttonLoading) {
        if (buttonLoading) {
            delay(DEMO_LOADING_MILLIS)
            buttonLoading = false
        }
    }
    LaunchedEffect(iconLoading) {
        if (iconLoading) {
            delay(DEMO_LOADING_MILLIS)
            iconLoading = false
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButton(
                label = "Refresh",
                onClick = { buttonLoading = true },
                style = TvButtonStyle.Primary,
                leadingIcon = Icons.Filled.Refresh,
                loading = buttonLoading,
            )
            TvIconButton(icon = Icons.Filled.Add, label = "Add to list", onClick = { iconLoading = true }, loading = iconLoading)
        }
    }
}
