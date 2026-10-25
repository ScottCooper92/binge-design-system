package com.binge.designsystem.tv.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageResult
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.coroutines.EmptyCoroutineContext

/** Two rows of four: what a TV panel composes before the grid scrolls. */
private const val CELLS = 8

/**
 * The shared v2 poster grid: focus enters on the cell it is told to, D-pad moves report the index they land on, OK opens the
 * cell, and while loading it draws plates that cannot take focus. Posters carry no art, so each draws its title.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvPosterGridTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill a Robolectric run, so no image loads at all. */
    @get:Rule
    val imageLoaderRule: TestRule = NoImagesRule()

    private val entry = FocusRequester()
    private val focusedCells = mutableListOf<Int>()
    private val opened = mutableListOf<Int>()

    @Test
    fun `entry focus lands on the entry cell`() {
        setGrid(entryIndex = 5)

        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title 5").assertIsFocused()
    }

    @Test
    fun `an entry index past the end lands on the last cell`() {
        setGrid(entryIndex = CELLS + 10)

        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title ${CELLS - 1}").assertIsFocused()
    }

    @Test
    fun `moving down a row reports the cell it lands on`() {
        setGrid(entryIndex = 1)
        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title 1").performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title 5").assertIsFocused()
        assertEquals(listOf(1, 5), focusedCells)
    }

    @Test
    fun `OK opens the focused cell`() {
        setGrid(entryIndex = 2)
        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title 2").performKeyInput { pressKey(Key.DirectionCenter) }

        assertEquals(listOf(2), opened)
    }

    @Test
    fun `while loading the plates take no focus`() {
        setGrid(entryIndex = 0, loading = true)

        composeTestRule.onAllNodes(isFocusable()).assertCountEquals(0)
    }

    @Test
    fun `a cell with no poster yet draws as a plate and takes no focus`() {
        setGrid(entryIndex = 0, missing = setOf(3))

        composeTestRule.onAllNodes(isFocusable()).assertCountEquals(CELLS - 1)
    }

    @Test
    fun `while loading with no results the grid never asks for a poster past the count`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvPosterGrid(
                    count = 0,
                    posterAt = { index -> error("posterAt($index) is past the count, as LazyPagingItems.get would throw") },
                    loading = true,
                    entry = remember { entry },
                    entryIndex = 0,
                    listKey = Unit,
                    onCellFocused = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a stable-plate grid keeps the focused plate when the results end before it`() {
        var count by mutableIntStateOf(0)
        var loading by mutableStateOf(true)
        composeTestRule.setContent {
            BingeTvTheme {
                TvPosterGrid(
                    count = count,
                    posterAt = { index -> TvPosterCell(imageUrl = null, title = "Title $index", onClick = {}) },
                    loading = loading,
                    entry = remember { entry },
                    entryIndex = 0,
                    listKey = Unit,
                    onCellFocused = {},
                    stablePlates = true,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()
        repeat(2) {
            composeTestRule.onAllNodes(isFocusable() and isFocused()).onFirst().performKeyInput { pressKey(Key.DirectionDown) }
            composeTestRule.waitForIdle()
        }

        composeTestRule.runOnIdle {
            count = 3
            loading = false
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isFocusable() and isFocused()).assertCountEquals(1)
    }

    private fun setGrid(
        entryIndex: Int,
        loading: Boolean = false,
        missing: Set<Int> = emptySet(),
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvPosterGrid(
                    count = CELLS,
                    posterAt = { index ->
                        TvPosterCell(imageUrl = null, title = "Title $index", onClick = { opened += index }).takeIf { index !in missing }
                    },
                    loading = loading,
                    entry = remember { entry },
                    entryIndex = entryIndex,
                    listKey = Unit,
                    onCellFocused = { focusedCells += it },
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}

/** Fails every Coil request on the calling thread for the test's duration, so a poster takes its error branch and draws its title. */
internal class NoImagesRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                SingletonImageLoader.setUnsafe { context ->
                    ImageLoader
                        .Builder(context)
                        .components { add(NoImages) }
                        .interceptorCoroutineContext(EmptyCoroutineContext)
                        .fetcherCoroutineContext(EmptyCoroutineContext)
                        .decoderCoroutineContext(EmptyCoroutineContext)
                        .build()
                }
                try {
                    base.evaluate()
                } finally {
                    SingletonImageLoader.reset()
                }
            }
        }
}

private object NoImages : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult =
        ErrorResult(image = null, request = chain.request, throwable = UnsupportedOperationException("No images in tests"))
}
