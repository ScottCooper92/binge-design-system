@file:CatalogGroup("Choices")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvChoiceRow
import com.binge.designsystem.tv.component.TvChoiceUi
import com.binge.designsystem.tv.component.TvOptionGroup
import com.binge.designsystem.tv.component.TvRatingStepper
import com.binge.designsystem.tv.component.TvTabCount
import com.binge.designsystem.tv.component.TvTabRow
import com.binge.designsystem.tv.component.TvTabUi
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/*
 * The TV controls that choose: a stacked option group, a checkbox row, a pill choice row where OK commits, and a tab
 * row where focus does. Focus is seeded through each control's initial-focus parameter, so the lit state is captured
 * without a focus request. Each sample keeps its own state, seeded with what its frame shows.
 */

private const val KEY_MOVIES = "movies"
private const val KEY_TV = "tv"
private const val KEY_EPISODES = "episodes"
private const val SAMPLE_MOVIE_COUNT = 88
private const val SAMPLE_SHOW_COUNT = 54

/** A stacked option group: the chosen option ticked in the accent, focus on another option filling it. */
@Composable
fun TvOptionGroupSample() {
    var selected by remember { mutableStateOf("/data/movies") }
    TvOptionGroup(
        title = "Root folder",
        choices = listOf("/data/movies", "/data/movies-4k", "/mnt/archive/films").map { it to it },
        selected = selected,
        onSelect = { selected = it },
        initialFocusedLabel = "/data/movies-4k",
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
    )
}

/** The checkbox row ticked and focused, unticked at rest, and disabled. */
@Composable
fun TvCheckboxRowSample() {
    var agreed by remember { mutableStateOf(true) }
    var keepServer by remember { mutableStateOf(false) }
    Column(
        modifier =
            Modifier
                .padding(dimensionResource(DesR.dimen.padding_l))
                .width(dimensionResource(TvR.dimen.tv_text_field_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        TvCheckboxRow(label = "Allow plain HTTP", checked = agreed, onCheckedChange = { agreed = it }, initiallyFocused = true)
        TvCheckboxRow(label = "Remember this server", checked = keepServer, onCheckedChange = { keepServer = it })
        TvCheckboxRow(label = "Not available here", checked = false, onCheckedChange = {}, enabled = false)
    }
}

/** A pill choice row where OK commits: Movies chosen and ticked, focus resting on TV shows without choosing it. */
@Composable
fun TvChoiceRowSample() {
    var selected by remember { mutableStateOf(KEY_MOVIES) }
    TvChoiceRow(
        choices = listOf(TvChoiceUi(KEY_MOVIES, "Movies"), TvChoiceUi(KEY_TV, "TV shows")),
        selectedKey = selected,
        onSelect = { selected = it },
        initialFocusedKey = KEY_TV,
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
    )
}

/** A tab row with focus gone down to what it shows: the selected tab in the dim accent, the others at rest. */
@Composable
fun TvTabRowSample() {
    SampleTabRow(tabs = LibraryTabs, initiallyFocused = false)
}

/** Focus back in the tab row, on the selected tab: the full fill. Focus and selection are always the same tab. */
@Composable
fun TvTabRowFocusedSample() {
    SampleTabRow(tabs = LibraryTabs, initiallyFocused = true)
}

/** Three tabs whose counts have not arrived: each count holds its floor width, so nothing moves when they land. */
@Composable
fun TvTabRowCountsUnknownSample() {
    SampleTabRow(tabs = RatedTabs.map { it.copy(count = TvTabCount.Unknown) }, initiallyFocused = false)
}

/** A tab row with no counts at all: no pill, and none reserved. */
@Composable
fun TvTabRowWithoutCountsSample() {
    SampleTabRow(tabs = RatedTabs.map { it.copy(count = TvTabCount.None) }, initiallyFocused = false)
}

@Composable
private fun SampleTabRow(tabs: List<TvTabUi>, initiallyFocused: Boolean) {
    var selected by remember { mutableStateOf(KEY_MOVIES) }
    TvTabRow(
        tabs = tabs,
        selectedKey = selected,
        onSelect = { selected = it },
        initiallyFocused = initiallyFocused,
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
    )
}

private val LibraryTabs =
    listOf(
        TvTabUi(key = KEY_MOVIES, label = "Movies", count = TvTabCount.Known(SAMPLE_MOVIE_COUNT)),
        TvTabUi(key = KEY_TV, label = "TV shows", count = TvTabCount.Known(SAMPLE_SHOW_COUNT)),
    )

private val RatedTabs = LibraryTabs + TvTabUi(key = KEY_EPISODES, label = "Episodes")

/** The rating stepper at a whole-star value, resting: filled stars in the accent, the rest empty. */
@Composable
fun TvRatingStepperSample() {
    SampleRatingStepper(initial = RATING_WHOLE, initiallyFocused = false)
}

/** An odd value draws a half star: seven reads as three and a half stars beside the number. */
@Composable
fun TvRatingStepperHalfStarSample() {
    SampleRatingStepper(initial = RATING_HALF, initiallyFocused = false)
}

/** Nothing set yet: all five stars empty. */
@Composable
fun TvRatingStepperUnratedSample() {
    SampleRatingStepper(initial = 0, initiallyFocused = false)
}

/** Focus on the stepper: the ring shows, and ← → would step the value. */
@Composable
fun TvRatingStepperFocusedSample() {
    SampleRatingStepper(initial = RATING_HALF, initiallyFocused = true)
}

/** A rated stepper in the room a side sheet leaves it: the plate is the room, and the stars and the whole "8 / 10" sit inside it on one line. */
@Composable
fun TvRatingStepperInSheetSample() {
    Box(Modifier.wrapContentSize(Alignment.TopStart).padding(dimensionResource(DesR.dimen.padding_l))) {
        SampleRatingStepper(
            initial = RATING_WHOLE,
            initiallyFocused = false,
            modifier = Modifier
                .width(dimensionResource(TvR.dimen.tv_rating_stepper_sheet_room))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            padded = false,
        )
    }
}

@Composable
private fun SampleRatingStepper(
    initial: Int,
    initiallyFocused: Boolean,
    modifier: Modifier = Modifier,
    padded: Boolean = true,
) {
    var value by remember { mutableStateOf(initial) }
    TvRatingStepper(
        value = value,
        onValueChange = { value = it },
        valueLabel = "$value / $RATING_MAX",
        initiallyFocused = initiallyFocused,
        modifier = if (padded) modifier.padding(dimensionResource(DesR.dimen.padding_l)) else modifier,
    )
}

private const val RATING_WHOLE = 8
private const val RATING_HALF = 7
private const val RATING_MAX = 10
