package com.binge.designsystem.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextStyle
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A `titleContent` slot inherits the hero's own title style and colour, so a text fallback matches it (#542). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, shadows = [ShadowMeshSpecification::class])
class DetailHeroTitleSlotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the detail hero's title slot inherits the hero's title style and colour`() {
        var slotStyle: TextStyle? = null
        var slotColor: Color? = null
        var expectedStyle: TextStyle? = null
        var expectedColor: Color? = null
        rule.setContent {
            TestTheme {
                expectedStyle = DetailHeroDefaults.titleStyle()
                expectedColor = MaterialTheme.colorScheme.onBackground
                DetailHero(
                    title = "Title",
                    backdropUrl = null,
                    tagline = null,
                    metaText = "2024",
                    onBack = {},
                    showChrome = false,
                    titleContent = {
                        slotStyle = LocalTextStyle.current
                        slotColor = LocalContentColor.current
                    },
                )
            }
        }
        rule.waitForIdle()
        assertEquals(expectedStyle, slotStyle)
        assertEquals(expectedColor, slotColor)
    }

    @Test
    fun `the cinematic header's title slot inherits its title style`() {
        var slotStyle: TextStyle? = null
        var expectedStyle: TextStyle? = null
        rule.setContent {
            TestTheme {
                expectedStyle = DetailHeroDefaults.cinematicTitleStyle()
                DetailCinematicHeader(
                    title = "Title",
                    genres = emptyList(),
                    synopsis = null,
                    stats = emptyList(),
                    backdropUrl = null,
                    posterUrl = null,
                    titleContent = { slotStyle = LocalTextStyle.current },
                )
            }
        }
        rule.waitForIdle()
        assertEquals(expectedStyle, slotStyle)
    }
}
