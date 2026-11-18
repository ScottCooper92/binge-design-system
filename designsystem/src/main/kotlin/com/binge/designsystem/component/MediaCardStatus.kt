package com.binge.designsystem.component

import com.binge.designsystem.theme.BingeSentiment

/**
 * A short status on a [MediaCard] poster, such as where a request for the title stands: drawn as a dot in the [sentiment]'s colour at the
 * poster's top-end corner, with [label] announced as part of the card ("Arrival, Available") because a colour alone cannot say it.
 * The consumer owns the words: [label] is already translated. It is never drawn, so it may be as long as a screen reader needs.
 */
data class MediaCardStatus(
    val label: String,
    val sentiment: BingeSentiment,
)
