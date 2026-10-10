package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SYNOPSIS = "A spice war on a desert world."

/** A synopsis OK opens is a control, so it announces as a button (#472). */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = Application::class,
    qualifiers = "w960dp-h540dp-television-xhdpi",
    // The hero draws its backdrop wash through MeshSpecification, which Robolectric has no native for.
    shadows = [ShadowMeshSpecification::class],
)
class TvDetailHeroSemanticsTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `a pressable synopsis announces as a button`() {
        rule.setContent {
            BingeTvTheme {
                TvDetailHero(
                    item = TvDetailHeroItem(title = "Dune", overview = SYNOPSIS),
                    artwork = {},
                    poster = {},
                    overview = TvHeroOverview(isFocused = false, onFocusChanged = {}, onClick = {}),
                ) {}
            }
        }

        rule.onNodeWithText(SYNOPSIS).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun `a pressable synopsis says what OK does when it is given a label`() {
        rule.setContent {
            BingeTvTheme {
                TvDetailHero(
                    item = TvDetailHeroItem(title = "Dune", overview = SYNOPSIS),
                    artwork = {},
                    poster = {},
                    overview = TvHeroOverview(isFocused = false, onFocusChanged = {}, onClick = {}, onClickLabel = "read more"),
                ) {}
            }
        }

        rule.onNodeWithText(SYNOPSIS).assert(
            SemanticsMatcher("has the click label read more") { it.config.getOrNull(SemanticsActions.OnClick)?.label == "read more" },
        )
    }
}
