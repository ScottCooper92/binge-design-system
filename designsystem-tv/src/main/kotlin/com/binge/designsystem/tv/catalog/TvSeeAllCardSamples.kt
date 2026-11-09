@file:CatalogGroup("Page templates")
@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.component.TvSeeAllCard
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val GALLERY_ASPECT_RATIO = 16f / 9f
private const val SAMPLE_COUNT = 24

private val SamplePeek: List<@Composable BoxScope.() -> Unit> =
    listOf(Color(0xFF6B7FD7), Color(0xFF9B6BD7), Color(0xFFD76B9B)).map { tone ->
        { Box(Modifier.fillMaxSize().background(tone)) }
    }

/** The card that closes a poster row, at rest and focused, with three stand-in images fanned behind the label. */
@Composable
fun TvSeeAllCardPosterSample() {
    SeeAllCardPair(aspectRatio = CARD_ASPECT_RATIO, width = dimensionResource(TvR.dimen.tv_immersive_card_width))
}

/** The same card at 16:9, as it closes a gallery row. */
@Composable
fun TvSeeAllCardGallerySample() {
    SeeAllCardPair(aspectRatio = GALLERY_ASPECT_RATIO, width = dimensionResource(TvR.dimen.tv_immersive_card_width) * 2)
}

@Composable
private fun SeeAllCardPair(aspectRatio: Float, width: Dp) {
    var focused by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        TvSeeAllCard(
            label = stringResource(TvR.string.tv_see_all_count, SAMPLE_COUNT),
            isFocused = false,
            onFocusChanged = {},
            onClick = {},
            peek = SamplePeek,
            modifier = Modifier.width(width).aspectRatio(aspectRatio),
        )
        TvSeeAllCard(
            label = stringResource(TvR.string.tv_see_all_count, SAMPLE_COUNT),
            isFocused = focused,
            onFocusChanged = { focused = it },
            onClick = {},
            peek = SamplePeek,
            modifier = Modifier.width(width).aspectRatio(aspectRatio),
        )
    }
}
