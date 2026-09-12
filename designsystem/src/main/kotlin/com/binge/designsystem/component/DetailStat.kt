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
 * The sub-label under a title's rating: the vote count when TMDB has one, and the bare word
 * "Rating" when it does not — a stat reading "8.2 / 0 votes" is worse than one that says nothing
 * about how many people rated it.
 *
 * Here rather than in each detail feature because the movie and TV screens built it identically,
 * off the same two designsystem resources, differing only in which model they read the count from
 * (#1230).
 */
@Composable
fun ratingSubLabel(voteCount: Int): String =
    if (voteCount > 0) {
        pluralStringResource(R.plurals.detail_stat_votes, voteCount, voteCount.formatVoteCount())
    } else {
        stringResource(R.string.detail_rating)
    }
