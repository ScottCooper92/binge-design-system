package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.R
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A meta item that needs the whole line (a long genre list at a large font) still leaves room for the dot after it, so
 * the next item starts a new line behind a separator rather than with none.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MetaFlowDotTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `an item as wide as the line leaves room for its dot`() {
        var reserved = 0.dp
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                reserved = dimensionResource(R.dimen.hero_meta_gap) + dimensionResource(R.dimen.hero_meta_dot_size)
                Box(Modifier.width(200.dp).testTag("flow")) {
                    MetaFlow(
                        entries = listOf(
                            { Text("Acción · Crimen · Drama · Suspense · Ciencia ficción · Aventura") },
                            { Text("Next") },
                        ),
                    )
                }
            }
        }
        rule.waitForIdle()

        val flow = rule.onNodeWithTag("flow").getBoundsInRoot()
        val first = rule.onNodeWithText("Acción", substring = true).getBoundsInRoot()
        assertTrue(
            "the long item ends ${flow.right - first.right} short of the line's end; the dot needs $reserved",
            first.right <= flow.right - reserved,
        )
    }
}
