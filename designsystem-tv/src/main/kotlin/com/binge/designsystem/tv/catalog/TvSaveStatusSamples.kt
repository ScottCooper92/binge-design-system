@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.binge.designsystem.tv.R as TvR

/** The save status line in each state it draws: saving, saved, and a failure with Retry. Idle draws nothing. */
@Composable
fun TvSaveStatusStatesSample() {
    var failed by remember { mutableStateOf<SaveStatus>(SaveStatus.Failed) }
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        TvSaveStatus(status = SaveStatus.Saving, onRetry = {})
        TvSaveStatus(status = SaveStatus.Saved, onRetry = {})
        TvSaveStatus(status = failed, onRetry = { failed = SaveStatus.Saved })
    }
}

/** A failure in a narrow panel, as in a side sheet: the words wrap and Retry keeps its label. */
@Composable
fun TvSaveStatusNarrowSample() {
    Column(modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l))) {
        TvSaveStatus(
            status = SaveStatus.Failed,
            onRetry = {},
            modifier = Modifier.width(dimensionResource(TvR.dimen.tv_save_status_narrow_sample_width)),
        )
    }
}
