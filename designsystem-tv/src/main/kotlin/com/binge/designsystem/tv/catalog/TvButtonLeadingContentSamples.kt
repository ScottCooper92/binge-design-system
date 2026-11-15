package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.component.TvButtonSurface
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The button with a composable leading slot: an account's picture before its name, resting, focused and disabled, in the
 * secondary and primary styles, with a long name beside a short one.
 */
@Composable
fun TvButtonLeadingContentSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccountButton("Alexander Hamilton", TvButtonStyle.Secondary, enabled = true, isFocused = false)
            AccountButton("Alexander Hamilton", TvButtonStyle.Secondary, enabled = true, isFocused = true)
            AccountButton("Alexander Hamilton", TvButtonStyle.Secondary, enabled = false, isFocused = false)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccountButton("Ada", TvButtonStyle.Primary, enabled = true, isFocused = false)
            AccountButton("Ada", TvButtonStyle.Primary, enabled = true, isFocused = true)
        }
    }
}

@Composable
private fun AccountButton(
    name: String,
    style: TvButtonStyle,
    enabled: Boolean,
    isFocused: Boolean,
) {
    TvButtonSurface(
        label = name,
        style = style,
        enabled = enabled,
        isFocused = isFocused,
        leadingContent = { BingeTvInitialsAvatar(name = name, size = dimensionResource(TvR.dimen.tv_button_avatar)) },
    )
}
