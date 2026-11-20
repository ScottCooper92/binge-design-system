@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.template.TvAvatarGridItem
import com.binge.designsystem.tv.template.TvAvatarGridPage
import com.binge.designsystem.tv.template.TvAvatarGridState

private const val HEADING = "The Grand Budapest Hotel"
private const val UNCREDITED_ID = 12

private val Faces = listOf(
    "Ralph Fiennes" to "M. Gustave",
    "Tony Revolori" to "Zero Moustafa",
    "F. Murray Abraham" to "Mr. Moustafa",
    "Mathieu Amalric" to "Serge X.",
    "Adrien Brody" to "Dmitri",
    "Willem Dafoe" to "Jopling",
    "Jeff Goldblum" to "Deputy Kovacs",
    "Jude Law" to "Young Writer",
    "Bill Murray" to "M. Ivan",
    "Edward Norton" to "Henckels",
    "Saoirse Ronan" to "Agatha",
    "Extra Player" to "",
).mapIndexed { index, (name, role) ->
    TvAvatarGridItem(key = index + 1, name = name, role = role.ifBlank { null }, imageUrl = null)
}

@Composable
private fun SampleAvatarGrid(
    state: TvAvatarGridState<Int>,
    initialFocusedKey: Int? = null,
    errorRetryFocused: Boolean = false,
) {
    TvAvatarGridPage(
        heading = HEADING,
        state = state,
        onItemClick = {},
        initialFocusedKey = initialFocusedKey,
        errorRetryFocused = errorRetryFocused,
    )
}

/** The heading over a grid of circular faces, the first focused. The fixtures carry no artwork, so every face is its person-glyph plate. */
@Composable
fun TvAvatarGridPageSample() {
    SampleAvatarGrid(TvAvatarGridState.Ready(Faces), initialFocusedKey = 1)
}

/** The last face has no role, so its caption slot renders empty yet stays a card-height tall: a short card in an even row shows here first. */
@Composable
fun TvAvatarGridPageBlankRoleSample() {
    SampleAvatarGrid(TvAvatarGridState.Ready(Faces), initialFocusedKey = UNCREDITED_ID)
}

/** Loading: two rows of six circle plates with their caption lines, the shape the grid resolves to. */
@Composable
fun TvAvatarGridPageLoadingSample() {
    SampleAvatarGrid(TvAvatarGridState.Loading)
}

/** A failure with Try again. */
@Composable
fun TvAvatarGridPageErrorSample() {
    SampleAvatarGrid(TvAvatarGridState.Error(ErrorKind.Network, onRetry = {}))
}

/** The Try again button focused: the failure's only focus stop. */
@Composable
fun TvAvatarGridPageErrorFocusedSample() {
    SampleAvatarGrid(TvAvatarGridState.Error(ErrorKind.Network, onRetry = {}), errorRetryFocused = true)
}

/** A failure the caller cannot retry: the message is the whole plate, with no focus stop. */
@Composable
fun TvAvatarGridPageErrorNoRetrySample() {
    SampleAvatarGrid(TvAvatarGridState.Error(ErrorKind.Network))
}

/** Nothing to show, said in the caller's words. */
@Composable
fun TvAvatarGridPageEmptySample() {
    SampleAvatarGrid(TvAvatarGridState.Empty("No cast is listed for this title yet."))
}
