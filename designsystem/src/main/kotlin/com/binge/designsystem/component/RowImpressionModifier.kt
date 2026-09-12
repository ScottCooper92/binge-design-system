package com.binge.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Reports this node as it reaches the viewport — the non-lazy counterpart to [RowImpressionEffect].
 *
 * The detail screens need their own shape because their body is `Column(Modifier.verticalScroll(…))`,
 * a `ScrollState` with **no `layoutInfo` at all**: there is no list of visible items to read, so the
 * only thing that knows whether a rail is on screen is the rail's own placement (#2269).
 *
 * Visibility is `boundsInWindow()` being non-empty, which is the intersection of this node with every
 * clip above it — so a rail scrolled out of the column measures empty even though it is still composed
 * and still has a size. "Reached the viewport" is any part of it, the same bar [RowImpressionEffect]
 * sets and for the same reason: a rail the user scrolled to the edge of and turned back from is one
 * they saw.
 *
 * Reports every time the node is positioned while visible — which is once per scroll frame, not once.
 * Deduplication is the caller's, exactly as it is for [RowImpressionEffect], because only the caller
 * knows what window "once" is measured over.
 */
fun Modifier.rowImpression(key: String, onRowVisible: (key: String) -> Unit): Modifier =
    onGloballyPositioned { coordinates ->
        if (coordinates.isAttached && !coordinates.boundsInWindow().isEmpty) onRowVisible(key)
    }
