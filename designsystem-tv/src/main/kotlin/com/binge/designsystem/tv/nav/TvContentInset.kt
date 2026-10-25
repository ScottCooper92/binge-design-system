package com.binge.designsystem.tv.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.R as TvR

/*
 * A shell's content pane is measured at the full panel width, so a surface is full-bleed by default: artwork and
 * lazy rows reach the panel edge. Content that must clear something the host draws at the start edge opts out with
 * padding built on [LocalTvContentInset].
 */

/**
 * The start inset in-pane content clears the host's start-edge chrome by. It is `0` unless a host provides it, so an
 * overlay-hosted screen and a preview are un-inset with nothing to do.
 */
val LocalTvContentInset = compositionLocalOf { 0.dp }

/**
 * The start inset plus the standard content gutter: how far an in-pane surface's first column of content clears the
 * panel edge. With no inset provided it is the bare gutter.
 */
@Composable
fun tvContentGutterStart(): Dp = LocalTvContentInset.current + dimensionResource(TvR.dimen.tv_content_gutter_start)
