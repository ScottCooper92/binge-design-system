package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The actions slot of [DetailOverlayTopBar] gives its actions the bar's tone and tint, as the other bars do, so a
 * [BingeBarIconAction] there is the same glass circle as Back, its glyph white over the hero and the theme's colour once the bar has
 * faded in.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailOverlayTopBarActionToneTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var tone: IconButtonTone? = null
    private var tint: Color = Color.Unspecified
    private var expectedTint: (Float) -> Color = { Color.Unspecified }

    private fun show(scrolledPast: (heroHeightPx: Float) -> Float) =
        rule.setContent {
            TestTheme {
                val heroPx = with(LocalDensity.current) { HERO_HEIGHT.toPx() }
                val onScrim = BingeTheme.colors.onScrim
                val onBackground = MaterialTheme.colorScheme.onBackground
                expectedTint = { progress -> lerp(onScrim, onBackground, progress) }
                Box(Modifier.fillMaxSize()) {
                    DetailOverlayTopBar(
                        title = "Alpha",
                        scrollOffsetPx = { scrolledPast(heroPx) },
                        onBack = null,
                        heroHeight = HERO_HEIGHT,
                        actions = {
                            tone = LocalTopBarActionTone.current
                            tint = LocalTopBarActionTint.current
                        },
                    )
                }
            }
        }

    @Test
    fun `over the hero the actions are glass circles with a white glyph`() {
        show { 0f }
        rule.waitForIdle()

        assertEquals(IconButtonTone.Glass, tone)
        assertEquals(expectedTint(0f), tint)
    }

    @Test
    fun `once the bar has filled the glyph is the theme's colour`() {
        show { heroPx -> heroPx * 2 }
        rule.waitForIdle()

        assertEquals(IconButtonTone.Glass, tone)
        assertEquals(expectedTint(1f), tint)
    }
}

private val HERO_HEIGHT = 300.dp
