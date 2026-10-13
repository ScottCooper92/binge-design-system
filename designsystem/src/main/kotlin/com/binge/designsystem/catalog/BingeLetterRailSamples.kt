@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLetterRail
import com.binge.designsystem.preview.ScreenshotTheme

/** The rail on its own, with M lit as the letter at the top of its list. A tap in the catalog lights another. */
@Composable
fun BingeLetterRailSample() {
    ScreenshotTheme {
        var current by remember { mutableStateOf<Char?>('M') }
        BingeLetterRail(
            letters = ('A'..'Z').toList(),
            onLetter = { current = it },
            current = current,
            modifier = Modifier.height(dimensionResource(R.dimen.letter_rail_sample_height)),
        )
    }
}

/** The rail in a short sheet: too short for every letter, so it shows every second one with a dot for each left out. */
@Composable
fun BingeLetterRailShortSample() {
    ScreenshotTheme {
        var current by remember { mutableStateOf<Char?>('M') }
        BingeLetterRail(
            letters = ('A'..'Z').toList() + '#',
            onLetter = { current = it },
            current = current,
            modifier = Modifier.height(dimensionResource(R.dimen.letter_rail_short_sample_height)),
        )
    }
}
