package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.tv.catalog.TvMessagePlateInBoxSample
import com.binge.designsystem.tv.catalog.TvMessagePlateInIntrinsicMaxSample
import com.binge.designsystem.tv.catalog.TvMessagePlateInIntrinsicMinSample
import com.binge.designsystem.tv.catalog.TvMessagePlateInLazyColumnSample
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A message plate lays out wherever a screen puts it (#1138 broke three such screens): under a lazy list, under a parent that asks for
 * an intrinsic size, and filling a box. It never asks for an intrinsic size itself and never subcomposes, so none of these throws.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvMessagePlateHostTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun assertRenders(content: @Composable () -> Unit) {
        rule.setContent { BingeTvTheme { Box(Modifier.fillMaxSize()) { content() } } }
        rule.waitForIdle()
        rule.onNodeWithText("Nothing here yet").assertIsDisplayed()
        rule.onNodeWithText("Refresh").assertIsDisplayed()
    }

    @Test
    fun `a plate in a LazyColumn item renders`() = assertRenders { TvMessagePlateInLazyColumnSample() }

    @Test
    fun `a plate under a parent with intrinsic min height renders`() = assertRenders { TvMessagePlateInIntrinsicMinSample() }

    @Test
    fun `a plate under a parent with intrinsic max height renders`() = assertRenders { TvMessagePlateInIntrinsicMaxSample() }

    @Test
    fun `a plate filling a box renders`() = assertRenders { TvMessagePlateInBoxSample() }
}
