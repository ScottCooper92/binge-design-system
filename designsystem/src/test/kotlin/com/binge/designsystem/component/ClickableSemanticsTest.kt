package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.paging.LoadState
import com.binge.designsystem.R
import com.binge.designsystem.template.PagedAppendFooter
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Every custom clickable says it is a button, and the ones with no text say what they do (#378). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ClickableSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    private val button = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    private fun string(id: Int, vararg args: Any) = RuntimeEnvironment.getApplication().getString(id, *args)

    private fun show(content: @Composable () -> Unit) = rule.setContent { BingeExpressiveTheme(dynamicColor = false) { content() } }

    @Test
    fun `the chips are buttons`() {
        show {
            Column {
                TrendingSearchChip(label = "Dune", onClick = {})
                DiscoverQuickClearChip(label = "Drama", tone = QuickClearTone.Include, onClear = {})
            }
        }

        rule.onNode(hasText("Dune") and button).assertExists()
        rule.onNode(hasText("Drama") and button).assertExists()
    }

    @Test
    fun `the include-exclude chip is a button, and names its long press`() {
        show {
            IncludeExcludeChip(
                label = "Horror",
                state = IncludeExcludeState.Neutral,
                onTap = {},
                onLongPress = {},
                onLongPressLabel = "Exclude",
            )
        }

        rule.onNode(hasText("Horror") and button).assert(
            SemanticsMatcher("long click labelled Exclude") {
                it.config.getOrNull(SemanticsActions.OnLongClick)?.label == "Exclude"
            },
        )
    }

    @Test
    fun `the rating card's edit toggle and reviews footer are buttons, and the toggle says whether it is open`() {
        show {
            RatingCard(
                userRating = 8f,
                isSignedIn = true,
                reviewCount = 3,
                averageReviewRating = 7.5f,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
        }
        val toggle = hasContentDescription(string(R.string.rating_card_change)) and button

        rule.onNode(toggle).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, string(R.string.cd_group_collapsed)))
        rule.onNode(toggle).performClick()
        rule.onNode(toggle).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, string(R.string.cd_group_expanded)))
        rule.onNode(hasText(string(R.string.rating_card_reviews_subtitle_no_average), substring = true) and button).assertExists()
    }

    @Test
    fun `the paged retry line is a button`() {
        show { PagedAppendFooter(state = LoadState.Error(RuntimeException()), retryLabel = "Retry", onRetry = {}) }

        rule.onNode(hasText("Retry") and button).assertExists()
    }

    @Test
    fun `the profile card's chevron is named after the card`() {
        show { AccountProfileCard(name = "Ana Lima", secondaryLine = "Member", initialsName = "Ana Lima", onClick = {}) }

        rule.onNodeWithContentDescription(string(R.string.cd_open_named, "Ana Lima")).assertExists()
    }

    @Test
    fun `the side sheet's scrim is a named button that closes it, and the panel is not a control`() {
        var dismissed = 0
        show { BingeModalSideSheet(onDismissRequest = { dismissed++ }) { Text("Filters") } }
        rule.waitForIdle()

        // The panel covers the scrim's centre on a narrow window, so the scrim's own click action stands in for a tap beside it.
        rule.onNode(hasContentDescription(string(R.string.cd_close_sheet)) and button).performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(1, dismissed)
        rule.onNodeWithText("Filters").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }
}
