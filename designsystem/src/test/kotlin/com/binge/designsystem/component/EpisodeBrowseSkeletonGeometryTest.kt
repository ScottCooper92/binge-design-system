package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [EpisodeBrowseRowSkeleton] reserves the height of a full [EpisodeBrowseRow] (a two-line name and a three-line synopsis),
 * so the rows below do not move when a season's episodes land. Size is checked as well as position.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class EpisodeBrowseSkeletonGeometryTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val anchor = LayoutAnchors.section("episode-browse-row")

    // Robolectric measures text with stand-in metrics that never wrap, so the line breaks are explicit.
    private val full = EpisodeBrowseUi(
        number = 1,
        name = "A name that takes\ntwo full lines",
        overview = "A synopsis that takes\nthree full lines of the\nrow's text column",
        rating = 8.7f,
        runtimeMinutes = 73,
    )

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `on a phone the skeleton reserves a full row`() = assertReserved()

    private fun assertReserved() {
        rule.assertSkeletonReservesGeometry(anchors = listOf(anchor), checkSize = true) { resolved ->
            BingeExpressiveTheme {
                Box(Modifier.fillMaxSize()) {
                    if (resolved) {
                        EpisodeBrowseRow(episode = full, modifier = Modifier.layoutAnchor(anchor))
                    } else {
                        EpisodeBrowseRowSkeleton(modifier = Modifier.layoutAnchor(anchor))
                    }
                }
            }
        }
    }
}
