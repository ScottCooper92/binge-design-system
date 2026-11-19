package com.binge.designsystem.template

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.resolvedContentInset

/**
 * The margin of a hero page's body, the one Binge's detail pages follow: [HeroDetailScreen] measures the width it leaves the
 * body and provides [LocalHeroReadingMargin], the gap that centres a column of `content_max_width` in it, which is zero up to
 * that width. Every section adds it, at every width, so a window wider than the column gets one under the full-width hero, and
 * the title, series and person pages all start in the same place. A section takes it as its modifier, so a section that draws
 * nothing takes no space; the rails, which bleed to the window's edge, take [readingInset] instead.
 *
 * @need start a section of a hero page's body where the reading column starts, at any window width
 * @group detail
 * @replaces a hand-built horizontal padding that tracks the window's width
 */
@Composable
fun Modifier.readingMargin(): Modifier = padding(horizontal = LocalHeroReadingMargin.current)

/**
 * The inset a section or a rail starts at: the screen's own content inset (16, 24 or 32dp by width, as the section headers use), plus the reading margin, so a rail's first card lines up with the
 * column and the rail still bleeds to the window's edge.
 *
 * @need start a rail on a hero page at the column's edge while it bleeds to the window's
 * @group detail
 * @replaces a hand-built start padding that adds the reading margin
 */
@Composable
fun readingInset(): Dp = resolvedContentInset() + LocalHeroReadingMargin.current
