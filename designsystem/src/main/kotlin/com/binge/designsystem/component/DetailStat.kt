package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.formatVoteCount

data class DetailStat(
    val icon: ImageVector,
    val value: String,
    val label: String,
)

/**
 * The sub-label under a title's rating: the vote count when there is one, and the bare word
 * "Rating" when it does not — a stat reading "8.2 / 0 votes" is worse than one that says nothing
 * about how many people rated it.
 *
 * Shared so every detail screen builds it the same way, off the same two resources, whatever model
 * the count is read from.
 */
@Composable
fun ratingSubLabel(voteCount: Int): String =
    if (voteCount > 0) {
        pluralStringResource(R.plurals.detail_stat_votes, voteCount, voteCount.formatVoteCount())
    } else {
        stringResource(R.string.detail_rating)
    }
