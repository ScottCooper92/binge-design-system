package com.binge.designsystem.tv.template

/**
 * The one action a message state offers. A pair, not two nullable params, so "label with no handler" and the
 * reverse are both unwritable. One action, not a list: error and empty each have exactly one resolver.
 */
data class TvGridAction(
    val label: String,
    val onClick: () -> Unit,
)
