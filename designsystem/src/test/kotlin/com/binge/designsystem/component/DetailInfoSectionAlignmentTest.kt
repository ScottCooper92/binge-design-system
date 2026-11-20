package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.template.LocalHeroReadingMargin
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
 * [DetailInfoSection]'s heading starts where the page's reading column does, as the sections around it do, so on a wide window it is not
 * left at the window's edge while its facts sit in the column.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailInfoSectionAlignmentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun left(text: String) =
        rule
            .onNodeWithText(text)
            .getBoundsInRoot()
            .left.value

    private fun assertHeadingInColumn(margin: Int) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                CompositionLocalProvider(LocalHeroReadingMargin provides margin.dp) {
                    Column(Modifier.fillMaxWidth()) {
                        DetailInfoSection(
                            heading = "Details",
                            entries = listOf(DetailInfoEntry(Icons.Filled.Person, "Director", "A Director")),
                        )
                        Text("Column", Modifier.padding(start = readingInset()))
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(left("Column"), left("Details"), 0.5f)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `on a phone the heading starts at the content inset`() = assertHeadingInColumn(margin = 0)

    @Test
    @Config(qualifiers = "w1000dp-h800dp-xhdpi")
    fun `on a wide window the heading starts inside the reading column`() = assertHeadingInColumn(margin = 180)
}
