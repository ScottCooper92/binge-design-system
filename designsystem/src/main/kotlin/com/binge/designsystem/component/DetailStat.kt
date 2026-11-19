package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.formatVoteCount
import java.util.Locale

/**
 * One cell of a [DetailStatRow]. [accent], when specified, colours the cell's icon, value and label, so one stat can carry a
 * sentiment, such as a count waiting on someone. Unspecified, the cell takes the row's colours.
 */
data class DetailStat(
    val icon: ImageVector,
    val value: String,
    val label: String,
    val accent: Color = Color.Unspecified,
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
        pluralStringResource(
            R.plurals.detail_stat_votes,
            voteCount,
            voteCount.formatVoteCount(resourceLocale()),
        )
    } else {
        stringResource(R.string.detail_rating)
    }

/**
 * The locale the library's string resources resolve to for the current configuration, which is the language of the
 * words they supply: the device's language when the library ships it, the default (English) when it does not. A number
 * is formatted in this locale beside a word from the resources, so the two agree: a device in a language the library
 * does not ship reads "1.3k votes", where the device's own locale would put its own digits and unit beside an English
 * word. It reads the language tag from a resource, [R.string.resource_locale_tag], which resolves exactly as the
 * strings beside it do, so a language added with its own `values-xx` folder follows with no code change.
 */
@Composable
internal fun resourceLocale(): Locale = Locale.forLanguageTag(stringResource(R.string.resource_locale_tag))
