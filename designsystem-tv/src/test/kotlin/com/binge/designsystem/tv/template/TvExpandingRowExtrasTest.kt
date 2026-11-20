package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BAR = "bar"

/**
 * A row held open, as the first row is when focus sits in the top bar, fetches its title's extras without waiting for focus
 * to arrive, so its logo is already there when it does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingRowExtrasTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val requested = mutableListOf<Int>()

    @Test
    fun `a row held open fetches the open title and the next two before it is focused`() {
        setHub()
        assertEquals(listOf(FIRST, FIRST + 1, FIRST + 2), requested)
    }

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                    TvExpandingRowsHub(
                        rows = listOf(TvHubRow(key = "row", title = "Row", items = List(COUNT) { title(FIRST + it) })),
                        onItemClick = {},
                        titleExtras = { item ->
                            requested += item.key.removePrefix("movie_").toInt()
                            null
                        },
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val FIRST = 1
        const val COUNT = 5

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")
    }
}
