package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The people and poster rails and the one-line cast start their heading where a plain section header does, at every width. */
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
                    DetailPeopleLine("Line", listOf("c"), { it }, { DetailPerson("Cy", null, null) }, {})
                }
            }
        }
        rule.waitForIdle()

        listOf("People", "Posters", "Line").forEach { assertEquals(it, left("Header"), left(it), 0.5f) }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `at a phone width the rails start where a section header does`() = assertAligned()

    @Test
    @Config(qualifiers = "w1000dp-h800dp-xhdpi")
    fun `at an expanded width the rails start where a section header does`() = assertAligned()
}
