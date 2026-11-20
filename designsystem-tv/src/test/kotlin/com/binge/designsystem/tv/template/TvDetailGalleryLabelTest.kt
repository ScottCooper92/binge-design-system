package com.binge.designsystem.tv.template

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What a screen reader says for each image of the gallery strip: the caller's description, or "Image 3 of 12". */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailGalleryLabelTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    @get:Rule
    val imageLoader = MainThreadImageLoaderRule()

    @Test
    fun `an image with no description is named by its place in the gallery`() {
        val images = List(3) { TvDetailGalleryImage("u$it", portrait = false, index = it) }
        composeTestRule.setContent { BingeTvTheme { TvDetailGalleryGrid(images = images, onOpen = {}) } }

        composeTestRule.onNodeWithContentDescription("Image 1 of 3").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Image 3 of 3").assertIsDisplayed()
    }

    @Test
    fun `the total is the whole gallery's when the strip holds only some of it`() {
        val images = listOf(TvDetailGalleryImage("u", portrait = false, index = 4))
        composeTestRule.setContent { BingeTvTheme { TvDetailGalleryGrid(images = images, onOpen = {}, totalCount = 12) } }

        composeTestRule.onNodeWithContentDescription("Image 5 of 12").assertIsDisplayed()
    }

    @Test
    fun `a description the caller gives replaces the default`() {
        val images = listOf(TvDetailGalleryImage("u", portrait = false, index = 0, description = "Poster with the lead"))
        composeTestRule.setContent { BingeTvTheme { TvDetailGalleryGrid(images = images, onOpen = {}) } }

        composeTestRule.onNodeWithContentDescription("Poster with the lead").assertIsDisplayed()
    }
}
