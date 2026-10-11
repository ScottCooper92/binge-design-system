@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.component.TvSaveStatus
import com.binge.designsystem.R as DesR

/** The save status line in each state it draws: saving, saved, and a failure with Retry. Idle draws nothing. */
@Composable
fun TvSaveStatusStatesSample() {
    var retried by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        TvSaveStatus(status = SaveStatus.Saving)
        TvSaveStatus(status = SaveStatus.Saved)
        TvSaveStatus(status = if (retried) SaveStatus.Saved else SaveStatus.Failed(onRetry = { retried = true }))
    }
}
