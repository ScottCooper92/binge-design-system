package com.binge.designsystem.component

import com.binge.designsystem.theme.BingeSentiment

/**
 * A short status drawn on a [MediaCard] poster as a [StatusChip], such as where a request for the title
 * stands. The consumer owns the words: [label] is already translated.
 */
data class MediaCardStatus(
    val label: String,
    val sentiment: BingeSentiment,
)
