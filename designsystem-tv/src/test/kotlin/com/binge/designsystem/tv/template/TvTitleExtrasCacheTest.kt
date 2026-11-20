package com.binge.designsystem.tv.template

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.binge.designsystem.testing.createTakeDownComposeRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The hub's extras cache calls the loader it was last given, not the first one, and keeps what an earlier loader brought. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TvTitleExtrasCacheTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    private var loader by mutableStateOf<suspend (TvHubItem) -> TvTitleExtras?>({
        TvTitleExtras(logoUrl = "first/${it.key}", certification = null)
    })
    private lateinit var cache: TvTitleExtrasCache

    private fun item(key: String) = TvHubItem(key = key, posterUrl = null, title = key, rating = null)

    private fun host() {
        composeTestRule.setContent { cache = rememberTvTitleExtrasCache(loader) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a loader that changed is the one a later title is loaded with`() {
        host()
        runBlocking { cache.ensure(item("a")) }
        loader = { TvTitleExtras(logoUrl = "second/${it.key}", certification = null) }
        composeTestRule.waitForIdle()

        runBlocking { cache.ensure(item("b")) }

        assertEquals("second/b", cache["b"]?.logoUrl)
    }

    @Test
    fun `what an earlier loader brought is kept`() {
        host()
        runBlocking { cache.ensure(item("a")) }
        loader = { TvTitleExtras(logoUrl = "second/${it.key}", certification = null) }
        composeTestRule.waitForIdle()

        runBlocking { cache.ensure(item("a")) }

        assertEquals("first/a", cache["a"]?.logoUrl)
    }

    @Test
    fun `a title nobody loaded is not there`() {
        host()

        assertNull(cache["missing"])
    }
}
