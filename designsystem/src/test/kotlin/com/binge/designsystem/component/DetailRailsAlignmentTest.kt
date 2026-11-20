package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The people and poster rails, the one-line cast, the action row and the episodes section (header, subtitle and the state slot) start where a plain section header does, at every width. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailRailsAlignmentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun left(text: String) =
        rule
            .onNodeWithText(text)
            .getBoundsInRoot()
            .left.value

    private fun assertAligned() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Column(Modifier.fillMaxWidth()) {
                    SectionHeader(title = "Header")
                    DetailPeopleRail("People", listOf("a"), { it }, { DetailPerson("Ada", null, null) }, {})
                    DetailPosterRail("Posters", listOf("b"), { it }, { DetailPoster(null, "Bee", null) }, {})
                    DetailActions(
                        primary = { Text("Primary", it.testTag("primary")) },
                        roundActions = emptyList(),
                    )
                    DetailEpisodesSection(
                        title = "Episodes",
                        episodes = listOf(EpisodeRow(1, "Pilot", null)),
                        subtitle = "Subtitle line",
                    )
                    DetailEpisodesSection(
                        title = "Loading episodes",
                        episodes = emptyList(),
                        stateContent = { Box(Modifier.testTag("state").fillMaxWidth().height(10.dp)) },
                    )
                    DetailPeopleLine("Line", listOf("c"), { it }, { DetailPerson("Cy", null, null) }, {})
                }
            }
        }
        rule.waitForIdle()

        listOf("People", "Posters", "Line").forEach { assertEquals(it, left("Header"), left(it), 0.5f) }
        listOf("Episodes", "Subtitle line", "Loading episodes").forEach { assertEquals(it, left("Header"), left(it), 0.5f) }
        assertEquals(
            "the placeholder in the state slot",
            left("Header"),
            rule
                .onNodeWithTag("state")
                .getBoundsInRoot()
                .left.value,
            0.5f,
        )
        assertEquals(
            "the action row",
            left("Header"),
            rule
                .onNodeWithTag("primary")
                .getBoundsInRoot()
                .left.value,
            0.5f,
        )
    }

    @Test
    @Config(qualifiers = "w411dp-h1800dp-xhdpi")
    fun `at a phone width the rails start where a section header does`() = assertAligned()

    @Test
    @Config(qualifiers = "w700dp-h1800dp-xhdpi")
    fun `at a medium width the rails and the action row start where a section header does`() = assertAligned()

    @Test
    @Config(qualifiers = "w1000dp-h1800dp-xhdpi")
    fun `at an expanded width the rails start where a section header does`() = assertAligned()
}
