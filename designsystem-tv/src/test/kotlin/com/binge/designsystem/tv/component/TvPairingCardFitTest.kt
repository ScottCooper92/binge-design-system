package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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

/** The code gives up size before the lines around it do, in a pane that is the limit, and keeps it in one that is not. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvPairingCardFitTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val paneHeight = 330.dp

    @Composable
    private fun Card() =
        TvPairingCard(
            payload = "http://192.168.1.20:8080/link?code=4821",
            qrDescription = "Scan to sign in",
            instruction = "Scan with your phone, or open the address below",
            code = "PIN 4 8 2 1",
            address = "http://192.168.1.20:8080",
            qrSize = 400.dp,
        )

    @Test
    fun `the whole card fits a pane shorter than the code asked for`() {
        rule.setContent {
            BingeTvTheme {
                Box(modifier = Modifier.size(width = 600.dp, height = paneHeight), contentAlignment = Alignment.Center) { Card() }
            }
        }

        val address = rule.onNodeWithText("http://192.168.1.20:8080").getUnclippedBoundsInRoot()
        val code = rule.onNodeWithText("PIN 4 8 2 1").getUnclippedBoundsInRoot()

        assertTrue("The address ends at ${address.bottom}, below the $paneHeight pane", address.bottom <= paneHeight)
        assertTrue("The code's lines start at ${code.top}, so the card is not above the pane", code.top >= 0.dp)
    }

    @Test
    fun `the loading card keeps its message inside a short pane`() {
        rule.setContent {
            BingeTvTheme {
                Box(modifier = Modifier.size(width = 600.dp, height = paneHeight), contentAlignment = Alignment.Center) {
                    TvPairingCardLoading(
                        message =
                            "Getting a code to sign in with. This can take a moment on a slow connection, " +
                                "and it appears here when ready.",
                        qrSize = 400.dp,
                    )
                }
            }
        }

        val message = rule.onNodeWithText("Getting a code", substring = true).getUnclippedBoundsInRoot()

        assertTrue("The message ends at ${message.bottom}, below the $paneHeight pane", message.bottom <= paneHeight)
    }

    @Test
    fun `the code keeps its full size in a scrolling host`() {
        rule.setContent {
            BingeTvTheme {
                Column(modifier = Modifier.size(width = 600.dp, height = paneHeight).verticalScroll(rememberScrollState())) { Card() }
            }
        }

        val instruction = rule.onNodeWithText("Scan with your phone", substring = true).getUnclippedBoundsInRoot()

        assertTrue("The instruction starts at ${instruction.top}, so the code is under 400dp", instruction.top >= 400.dp)
    }
}
