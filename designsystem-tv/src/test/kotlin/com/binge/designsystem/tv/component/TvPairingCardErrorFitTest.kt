package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A long failure in a pane that is the limit keeps its Retry inside the card instead of pushing it out (#613). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvPairingCardErrorFitTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val paneHeight = 230.dp

    @Test
    fun `retry keeps its height and stays inside a pane shorter than the code's box`() {
        rule.setContent {
            BingeTvTheme {
                Box(modifier = Modifier.size(width = 600.dp, height = paneHeight), contentAlignment = Alignment.Center) {
                    TvPairingCardError(
                        message = "This TV isn't on a home network, so a phone can't reach it.",
                        retryLabel = "Try again",
                        onRetry = {},
                        qrSize = 200.dp,
                    )
                }
            }
        }

        val retry = rule.onNodeWithText("Try again").getUnclippedBoundsInRoot()
        val message = rule.onNodeWithText("This TV isn't", substring = true).getUnclippedBoundsInRoot()

        assertTrue("Retry ends at ${retry.bottom}, below the $paneHeight pane", retry.bottom <= paneHeight)
        assertTrue("Retry is squeezed to ${retry.bottom - retry.top}", retry.bottom - retry.top >= MIN_BUTTON_HEIGHT)
        assertTrue("The message ends at ${message.bottom}, under Retry's top at ${retry.top}", message.bottom <= retry.top)
    }

    private companion object {
        val MIN_BUTTON_HEIGHT = 40.dp
    }
}
