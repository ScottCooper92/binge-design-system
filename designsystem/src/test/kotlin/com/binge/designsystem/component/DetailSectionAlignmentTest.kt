package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.template.readingInset
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A detail page's sections start where the screen's section headers do, at every width: the overview and its skeleton at the content inset
 * (16, 24 or 32dp by width), and a rail's [readingInset] at the same inset, so on a wide window nothing sits 16dp left of its heading.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailSectionAlignmentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun left(text: String) =
        rule
            .onNodeWithText(text)
            .getBoundsInRoot()
            .left.value

    private fun showAll() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Column(Modifier.fillMaxWidth()) {
                    SectionHeader(title = "Header")
                    DetailOverviewSection(heading = "Overview", text = "A synopsis.")
                    Text("Rail", Modifier.padding(start = readingInset()))
                }
            }
        }
        rule.waitForIdle()
    }

    private fun assertAligned() {
        showAll()

        assertEquals(left("Header"), left("Overview"), 0.5f)
        assertEquals(left("Header"), left("Rail"), 0.5f)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `at a phone width the overview and a rail start where a section header does`() = assertAligned()

    @Test
    @Config(qualifiers = "w700dp-h1000dp-xhdpi")
    fun `at a medium width the overview and a rail start where a section header does`() = assertAligned()

    @Test
    @Config(qualifiers = "w1000dp-h800dp-xhdpi")
    fun `at an expanded width the overview and a rail start where a section header does`() = assertAligned()
}
