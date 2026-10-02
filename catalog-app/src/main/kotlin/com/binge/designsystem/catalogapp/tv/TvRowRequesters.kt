package com.binge.designsystem.catalogapp.tv

import androidx.compose.runtime.Stable
import androidx.compose.ui.focus.FocusRequester

/**
 * One [FocusRequester] per sample row, by sample id, so the shell can return focus to the row that
 * opened a sample. Created here, outside composition, which is what lint's `RememberInComposition`
 * asks of a requester.
 */
@Stable
class TvRowRequesters {
    private val byId = HashMap<String, FocusRequester>()

    operator fun get(id: String): FocusRequester = byId.getOrPut(id) { FocusRequester() }
}
