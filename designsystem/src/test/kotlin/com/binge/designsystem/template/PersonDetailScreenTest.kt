package com.binge.designsystem.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.binge.designsystem.component.PersonFact
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private fun cards(prefix: String, count: Int) =
    List(count) { GridItem(key = "$prefix$it", posterUrl = null, title = "$prefix title $it", rating = 7f, subtitle = "Role $it") }

private fun person(credits: CreditsState, biography: String? = "A short biography.") =
    PersonDetail(
        name = "Maya Rodrigues",
        eyebrow = "Acting",
        facts = listOf(PersonFact("14 March 1985", "Born"), PersonFact("Also known as Maya R.")),
        biography = biography,
        credits = credits,
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PersonDetailScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        state: PersonDetailState,
        callbacks: PersonDetailCallbacks = PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {}),
        fontScale: Float = 1f,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme { PersonDetailScreen(state = state, callbacks = callbacks) }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `the profile, the biography and a credit are drawn, and a tap hands back the credit's key`() {
        var opened: String? = null
        show(
            PersonDetailState.Content(person(CreditsState.Ready(listOf(CreditSection("a", "Acting", cards("a", 3)))))),
            PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = { opened = it }),
        )
        rule.onAllNodesWithText("Maya Rodrigues").onFirst().assertExists()
        rule.onNodeWithText("ACTING", ignoreCase = false, substring = false).assertExists()
        rule.onNodeWithText("Biography").assertIsDisplayed()
        rule.onNodeWithText("A short biography.").assertIsDisplayed()
        rule.onNodeWithText("Acting").assertIsDisplayed()
        rule.onNodeWithText("a title 1", substring = true).performClick()
        assertEquals("a1", opened)
    }

    @Test
    fun `a section with no items draws no heading, and no biography draws no biography heading`() {
        show(
            PersonDetailState.Content(
                person(
                    CreditsState.Ready(listOf(CreditSection("a", "Acting", cards("a", 2)), CreditSection("c", "Crew", emptyList()))),
                    biography = null,
                ),
            ),
        )
        rule.onAllNodesWithText("Crew").assertCountEquals(0)
        rule.onAllNodesWithText("Biography").assertCountEquals(0)
    }

    @Test
    fun `a section that has more asks for its next page by index once, and none that has not`() {
        val asked = mutableListOf<Int>()
        show(
            PersonDetailState.Content(
                person(
                    CreditsState.Ready(
                        listOf(
                            CreditSection("a", "Acting", cards("a", 3), hasMore = true),
                            CreditSection("c", "Crew", cards("c", 3)),
                        ),
                    ),
                ),
            ),
            PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {}, onLoadMoreCredits = { asked += it }),
        )
        assertEquals(listOf(0), asked)
    }

    @Test
    fun `a page that added nothing asks again while the row still has more`() {
        val asked = mutableListOf<Int>()
        var section by mutableStateOf(CreditSection("a", "Acting", cards("a", 3), hasMore = true))
        rule.setContent {
            TestTheme {
                PersonDetailScreen(
                    state = PersonDetailState.Content(person(CreditsState.Ready(listOf(section)))),
                    callbacks = PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {}, onLoadMoreCredits = { asked += it }),
                )
            }
        }
        rule.waitForIdle()
        assertEquals(listOf(0), asked)
        section = CreditSection("a", "Acting", cards("a", 3), hasMore = true, appending = true)
        rule.waitForIdle()
        assertEquals(listOf(0), asked)
        section = CreditSection("a", "Acting", cards("a", 3), hasMore = true)
        rule.waitForIdle()
        assertEquals(listOf(0, 0), asked)
    }

    @Test
    fun `no See all tile by default, and one that hands back the section's key when asked for`() {
        show(PersonDetailState.Content(person(CreditsState.Ready(listOf(CreditSection("a", "Acting", cards("a", 2)))))))
        rule.onAllNodesWithText("See all").assertCountEquals(0)
    }

    @Test
    fun `See all draws a tile and links the heading`() {
        var seen: String? = null
        show(
            PersonDetailState.Content(person(CreditsState.Ready(listOf(CreditSection("a", "Acting", cards("a", 2)))))),
            PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {}, onSeeAll = { seen = it }),
        )
        rule.onAllNodesWithText("See all").assertCountEquals(2)
        rule.onAllNodesWithText("See all").onFirst().performClick()
        assertEquals("a", seen)
        seen = null
        rule.onAllNodesWithText("See all").onLast().performClick()
        assertEquals("a", seen)
    }

    @Test
    fun `a person with no titles shows the app's message and no rows`() {
        show(PersonDetailState.Content(person(CreditsState.Empty("No titles yet."))))
        rule.onNodeWithText("No titles yet.").assertIsDisplayed()
    }

    @Test
    fun `a failed append draws Retry, which asks for the page again`() {
        var asked = 0
        show(
            PersonDetailState.Content(
                person(CreditsState.Ready(listOf(CreditSection("a", "Acting", cards("a", 2), hasMore = true, appendFailed = true)))),
            ),
            PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {}, onLoadMoreCredits = { asked++ }),
        )
        val before = asked
        rule.onNodeWithText("Couldn't load more. Tap to retry.").performClick()
        assertEquals(before + 1, asked)
    }

    @Test
    fun `failed credits keep the person and retry only the credits`() {
        var pageRetries = 0
        var creditRetries = 0
        show(
            PersonDetailState.Content(person(CreditsState.Failed())),
            PersonDetailCallbacks(onBack = {}, onRetry = { pageRetries++ }, onOpenCredit = {}, onRetryCredits = { creditRetries++ }),
        )
        rule.onAllNodesWithText("Maya Rodrigues").onFirst().assertExists()
        rule.onNodeWithText("Couldn't load their credits.").assertIsDisplayed()
        rule.onNodeWithText("Try again").performClick()
        assertEquals(0, pageRetries)
        assertEquals(1, creditRetries)
    }

    @Test
    fun `a failed page keeps the back control and retries the page`() {
        var retried = false
        show(
            PersonDetailState.Failed(),
            PersonDetailCallbacks(onBack = {}, onRetry = { retried = true }, onOpenCredit = {}),
        )
        rule.onNodeWithText("Try again").performClick()
        assertEquals(true, retried)
    }

    @Test
    fun `the skeleton stands where the loaded profile does`() {
        var state by mutableStateOf<PersonDetailState>(PersonDetailState.Loading(facts = 2))
        rule.setContent {
            TestTheme { PersonDetailScreen(state = state, callbacks = PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {})) }
        }
        rule.waitForIdle()
        val anchor = hasTestTag(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE))
        val loading = rule.onNode(anchor).fetchSemanticsNode().boundsInRoot
        state = PersonDetailState.Content(person(CreditsState.Loading))
        rule.waitForIdle()
        val loaded = rule.onNode(anchor).fetchSemanticsNode().boundsInRoot
        assertEquals(loaded, loading)
    }

    private fun assertUnclippedAt(scale: Float) {
        show(
            PersonDetailState.Content(person(CreditsState.Ready(listOf(CreditSection("a", "Acting", cards("a", 3)))))),
            fontScale = scale,
        )
        rule.assertNoTextClipped()
    }

    @Test
    fun `nothing is clipped at 1_3x font`() = assertUnclippedAt(1.3f)

    @Test
    fun `nothing is clipped at 2x font`() = assertUnclippedAt(2f)
}
