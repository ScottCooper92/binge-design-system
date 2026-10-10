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

    private val paneHeight = 540.dp

    @Test
    fun `the retry button stays inside a pane as tall as a TV, with a four line message and a code sized from the pane`() {
        rule.setContent {
            BingeTvTheme {
                Box(modifier = Modifier.size(width = 960.dp, height = paneHeight), contentAlignment = Alignment.Center) {
                    TvPairingCardError(
                        message = "This TV isn't on a Wi-Fi or wired home network, so a phone can't reach it. Type the address instead.",
                        retryLabel = "Try again",
                        onRetry = {},
                        qrSize = paneHeight * 0.6f,
                    )
                }
            }
        }

        val retry = rule.onNodeWithText("Try again").getUnclippedBoundsInRoot()

        assertTrue("Retry ends at ${retry.bottom}, below the $paneHeight pane", retry.bottom <= paneHeight)
    }
}
