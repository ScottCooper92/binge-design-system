package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.nav.LocalTvContentInset
import com.binge.designsystem.tv.nav.LocalTvHostedAsOverlay
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CARDS_PER_ROW = 8
private val CardWidth = 120.dp
private val RailWidth = 72.dp

private val Rows =
    listOf(
        TvHubRow(key = "trending", title = "Trending", items = List(CARDS_PER_ROW) { it }),
        TvHubRow(key = "popular", title = "Popular", items = List(CARDS_PER_ROW) { 100 + it }),
    )

private val Anchors = listOf(TvLayoutAnchors.HUB_COPY, TvLayoutAnchors.hubRow(0), TvLayoutAnchors.hubRow(1))

/**
 * [TvImmersiveHubSkeleton] lays its copy band and rows out where [TvImmersiveHub] puts them, so nothing moves when
 * the content arrives (#316). Each hosting gets the start inset its host provides: the rail's width in its pane,
 * nothing above the shell or before it. An overlay and a pre-shell page also place focus at once, which anchors
 * the first row higher than it rests in the pane.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the skeleton reserves the hub's copy band and rows in the rail's pane`() = assertReserved(TvPageHosting.RailDestination)

    @Test
    fun `the skeleton reserves the hub's copy band and rows as an overlay`() = assertReserved(TvPageHosting.Overlay)

    @Test
    fun `the skeleton reserves the hub's copy band and rows before the shell`() = assertReserved(TvPageHosting.PreShell)

    private fun assertReserved(hosting: TvPageHosting) =
        composeTestRule.assertSkeletonReservesGeometry(Anchors, checkStartEdge = true) { resolved ->
            BingeTvTheme {
                CompositionLocalProvider(
                    LocalTvContentInset provides if (hosting == TvPageHosting.RailDestination) RailWidth else 0.dp,
                    LocalTvHostedAsOverlay provides (hosting == TvPageHosting.Overlay),
                ) {
                    if (resolved) Hub(hosting) else Skeleton(hosting)
                }
            }
        }
}

@Composable
private fun Hub(hosting: TvPageHosting) {
    TvImmersiveHub(
        rows = Rows,
        itemId = { it },
        cardWidth = CardWidth,
        onItemClick = {},
        artwork = {},
        copy = {},
        hosting = hosting,
    ) { _, _, onFocusChanged, onClick, cellModifier ->
        Box(cellModifier.width(CardWidth).aspectRatio(2f / 3f).tvClickable(onFocusChanged = onFocusChanged, onClick = onClick))
    }
}

@Composable
private fun Skeleton(hosting: TvPageHosting) =
    TvImmersiveHubSkeleton(hosting = hosting, rows = Rows.size, cardsPerRow = CARDS_PER_ROW, cardWidth = CardWidth)
