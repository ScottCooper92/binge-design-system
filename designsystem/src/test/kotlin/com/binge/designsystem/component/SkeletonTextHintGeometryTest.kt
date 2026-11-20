package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
import org.robolectric.annotation.GraphicsMode

private const val FONT_13 = 1.3f
private const val FONT_20 = 2f

/**
 * A skeleton with a text hint reserves what that text takes, at the default font and at 1.3x and 2x, where the words wrap over more lines.
 *
 * This proves the mechanism: the plate lays the hinted string out in the real style and the column's own width, so it measures the same as the
 * loaded text does. It does not prove an app's hint matches its data, which is the app's to choose. With no hint a skeleton is one line, as before.
 *
 * `NATIVE` graphics because the loaded text wraps, which Robolectric's legacy mode never does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h4000dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SkeletonTextHintGeometryTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val profileAnchor = LayoutAnchors.section(LayoutAnchors.Detail.PROFILE)
    private val rowAnchor = LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)

    private val eyebrow = "Acting"
    private val name = "Maria Fernanda Rodriguez"
    private val facts =
        listOf(
            PersonFact("Santiago de Compostela, Galicia, Spain", "Place of birth"),
            PersonFact("14 March 1971", "Born"),
            PersonFact("Actress, Producer", "Known for"),
        )
    private val title = "The Lord of the Rings: The Fellowship of the Ring"

    @Composable
    private fun scaled(fontScale: Float, content: @Composable () -> Unit) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
            BingeExpressiveTheme { Box(Modifier.width(412.dp)) { content() } }
        }
    }

    private fun profile(fontScale: Float) {
        rule.assertSkeletonReservesGeometry(anchors = listOf(profileAnchor), checkSize = true) { resolved ->
            scaled(fontScale) {
                if (resolved) {
                    PersonProfileHeader(name = name, photoUrl = null, eyebrow = eyebrow, facts = facts)
                } else {
                    PersonProfileSkeleton(expectedEyebrow = eyebrow, expectedName = name, expectedFacts = facts)
                }
            }
        }
    }

    private fun row(fontScale: Float) {
        rule.assertSkeletonReservesGeometry(anchors = listOf(rowAnchor), checkSize = true) { resolved ->
            scaled(fontScale) {
                val result = MediaResult("k", MediaResultKind.Poster, null, title, "Movie · 2001", rating = 8.4f)
                if (resolved) {
                    MediaResultRow(result, onClick = {}, modifier = Modifier.layoutAnchor(rowAnchor))
                } else {
                    MediaResultRowSkeleton(
                        modifier = Modifier.layoutAnchor(rowAnchor),
                        expectedName = title,
                    )
                }
            }
        }
    }

    @Test
    fun `a hinted profile skeleton reserves the profile at the default font`() = profile(1f)

    @Test
    fun `a hinted profile skeleton reserves the profile at 1_3`() = profile(FONT_13)

    @Test
    fun `a hinted profile skeleton reserves the profile at 2_0`() = profile(FONT_20)

    @Test
    fun `a hinted result row skeleton reserves the row at the default font`() = row(1f)

    @Test
    fun `a hinted result row skeleton reserves the row at 1_3`() = row(FONT_13)

    @Test
    fun `a hinted result row skeleton reserves the row at 2_0`() = row(FONT_20)

    private fun blurbRow(fontScale: Float) {
        rule.assertSkeletonReservesGeometry(anchors = listOf(rowAnchor), checkSize = true) { resolved ->
            scaled(fontScale) {
                val result = MediaResult("k", MediaResultKind.Poster, null, title, null, rating = 8.4f, blurb = "A short blurb.")
                if (resolved) {
                    MediaResultRow(result, onClick = {}, modifier = Modifier.layoutAnchor(rowAnchor))
                } else {
                    MediaResultRowSkeleton(modifier = Modifier.layoutAnchor(rowAnchor), blurbLines = 1, expectedName = title)
                }
            }
        }
    }

    @Test
    fun `a hinted name is honoured in a row with a blurb at 1_3`() = blurbRow(FONT_13)

    @Test
    fun `a hinted name is honoured in a row with a blurb at 2_0`() = blurbRow(FONT_20)
}
