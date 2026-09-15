package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for the button family (see the convention on [MediaCardRatedSample]). Each is a
 * no-arg `@Composable` wrapped in [ScreenshotTheme], called by both the catalog `@Preview` and the
 * matching screenshot test so the two renders are identical.
 */
@Composable
fun ButtonFamilySample() {
    ScreenshotTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeFilledButton(
                label = "Filled",
                leadingIcon = Icons.Filled.PlayArrow,
                onClick = {},
            )
            BingeOutlinedButton(label = "Outlined", onClick = {})
        }
    }
}

/** Filled button in its loading state — taps are swallowed and a spinner replaces the label. */
@Composable
fun FilledButtonLoadingSample() {
    ScreenshotTheme {
        BingeFilledButton(
            label = "Watch trailer",
            onClick = {},
            loading = true,
        )
    }
}

/**
 * A destructive outlined button beside an ordinary one. The pair is the sample: the tone only means
 * anything against the button it is not.
 */
@Composable
fun OutlinedButtonDestructiveSample() {
    ScreenshotTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeOutlinedButton(label = "Keep", onClick = {})
            BingeOutlinedButton(label = "Delete files", onClick = {}, destructive = true)
        }
    }
}
