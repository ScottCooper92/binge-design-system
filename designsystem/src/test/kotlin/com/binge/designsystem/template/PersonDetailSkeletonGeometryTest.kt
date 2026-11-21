package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.PersonDetailLoadingHintedSample
import com.binge.designsystem.catalog.PersonDetailLoadingSample
import com.binge.designsystem.catalog.PersonDetailReadySample
import com.binge.designsystem.component.PersonFact
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The person template's loading state holds the profile, the biography and the first credit rail's heading where the loaded page puts them,
 * and at the size it gives them, at the default font. Sizes are checked, so the graphics mode is native.
 *
 * At a larger font the profile's own name and facts wrap, over as many lines as that person's words need, and a plate cannot know how many
 * that is: at 1.3 the sample's profile is 30dp taller than its skeleton and at 2.0 it is 132dp taller, and everything under it moves by the
 * same amount. With no hint the large-font cases hold only what the words do not decide, the profile's own top. With the hints the loading
 * state takes (`expectedName`, `expectedFacts`), the skeleton holds the whole page at every scale: that proves the mechanism, and an app's hints
 * are its own to choose.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PersonDetailSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors =
        listOf(LayoutAnchors.Detail.PROFILE, LayoutAnchors.Detail.OVERVIEW, LayoutAnchors.Detail.CREDITS).map(LayoutAnchors::section)

    private fun check(
        anchors: List<String>,
        checkSize: Boolean,
        hinted: Boolean = false,
    ) {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = checkSize) { resolved ->
            when {
                resolved -> PersonDetailReadySample()
                hinted -> PersonDetailLoadingHintedSample()
                else -> PersonDetailLoadingSample()
            }
        }
    }

    @Test
    fun `the skeleton holds the profile, the biography and the first credit heading`() = check(anchors, checkSize = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds the profile's top at a font scale of 1 point 3`() =
        check(listOf(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE)), checkSize = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds the profile's top at a font scale of 2`() =
        check(listOf(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE)), checkSize = false)

    @Test
    fun `a hinted skeleton holds the page at the default font`() = check(anchors, checkSize = true, hinted = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `a hinted skeleton holds the profile, the biography and the credits at a font scale of 1 point 3`() =
        check(anchors, checkSize = true, hinted = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `a hinted skeleton holds the profile, the biography and the credits at a font scale of 2`() =
        check(anchors, checkSize = true, hinted = true)

    private val facts =
        listOf(
            PersonFact("14 March 1985 (41 years old)", "Born"),
            PersonFact("Lisbon, Portugal", "Place of birth"),
            PersonFact("Also known as Maya R."),
        )

    /** A person whose biography is [biography], with the credits still loading, against the hinted skeleton: only the biography's toggle can differ. */
    private fun checkBiography(biography: String, hasOverviewToggle: Boolean) {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = true) { resolved ->
            ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
                PersonDetailScreen(
                    state = if (resolved) {
                        PersonDetailState.Content(
                            PersonDetail(
                                name = "Maya Rodrigues",
                                eyebrow = "Acting",
                                facts = facts,
                                biography = biography,
                                credits = CreditsState.Loading,
                            ),
                        )
                    } else {
                        PersonDetailState.Loading(
                            expectedEyebrow = "Acting",
                            expectedName = "Maya Rodrigues",
                            expectedFacts = facts,
                            hasOverviewToggle = hasOverviewToggle,
                        )
                    },
                    callbacks = PersonDetailCallbacks(onBack = {}, onRetry = {}, onOpenCredit = {}),
                )
            }
        }
    }

    @Test
    fun `a biography that fits its lines holds no toggle`() = checkBiography(OVERVIEW_THREE_LINES, hasOverviewToggle = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `a biography that fits its lines holds no toggle at a font scale of 1 point 3`() =
        checkBiography(OVERVIEW_THREE_LINES_LARGE, hasOverviewToggle = false)
}
