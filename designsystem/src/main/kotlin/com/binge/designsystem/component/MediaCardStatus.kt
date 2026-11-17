package com.binge.designsystem.component

import com.binge.designsystem.theme.BingeSentiment

/**
 * A short status drawn on a [MediaCard] poster as a [StatusChip], such as where a request for the title
 * stands. The consumer owns the words: [label] is already translated. It stays on one line and is cut with an ellipsis where it does not
 * fit the poster, so a short label serves best.
 */
data class MediaCardStatus(
    val label: String,
    val sentiment: BingeSentiment,
)
