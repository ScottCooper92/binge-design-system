package com.binge.designsystem.component

import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

/**
 * The shared rating components format for the locale the library's string resources resolved to, not the device's
 * default. With the device default English and the configuration Spanish (shipped), a rating reads "7,5" and not "7.5".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, shadows = [ShadowMeshSpecification::class], qualifiers = "es")
class RatingLocaleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val defaultLocale = Locale.getDefault()

    @Before
    fun deviceIsEnglish() {
        Locale.setDefault(Locale.UK)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `the rating chip reads the resource locale's separator`() {
        rule.setContent { TestTheme { RatingChip(rating = 7.5f) } }

        rule.onNodeWithText("7,5").assertExists()
        rule.onNodeWithText("7.5").assertDoesNotExist()
    }

    @Test
    fun `the star rating announces the resource locale's separator`() {
        rule.setContent { TestTheme { StarRating(rating = 7.5f) } }

        rule.onNodeWithContentDescription("7,5", substring = true).assertContentDescriptionContains("7,5", substring = true)
    }

    @Test
    fun `the episode row rating reads the resource locale's separator`() {
        rule.setContent {
            TestTheme { EpisodeListRow(episodeNumber = 1, name = "Pilot", stillUrl = null, onClick = null, rating = 7.5f) }
        }

        rule.onNodeWithText("7,5", substring = true).assertExists()
    }

    @Test
    fun `the hero rating reads the resource locale's separator`() {
        rule.setContent {
            TestTheme {
                HeroCarousel(
                    items = listOf(HeroItem(id = 1, imageUrl = null, title = "Title", rating = 7.5f)),
                    onItemClick = {},
                )
            }
        }

        rule.onNodeWithText("7,5", substring = true, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `the device default does not matter when the configuration is Spanish`() {
        Locale.setDefault(Locale.US)
        rule.setContent { TestTheme { RatingChip(rating = 7.5f) } }

        rule.onNodeWithText("7,5").assertExists()
    }
}

/**
 * A language the library does not ship takes the English words, so the rating takes the English separator beside them,
 * as the vote count does, whatever the configuration's own separator is.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "de")
class RatingUnshippedLocaleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val defaultLocale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `an unshipped configuration reads a point`() {
        Locale.setDefault(Locale.GERMANY)
        rule.setContent { TestTheme { RatingChip(rating = 7.5f) } }

        rule.onNodeWithText("7.5").assertExists()
    }

    @Test
    fun `the star rating announces a point beside the English words`() {
        rule.setContent { TestTheme { StarRating(rating = 7.5f) } }

        rule.onNodeWithContentDescription("7.5", substring = true).assertContentDescriptionContains("7.5", substring = true)
    }
}

/** With an English configuration a rating reads a point, even when the device default is Spanish. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "en")
class RatingEnglishLocaleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val defaultLocale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `an English configuration reads a point`() {
        Locale.setDefault(Locale("es", "ES"))
        rule.setContent { TestTheme { RatingChip(rating = 7.5f) } }

        rule.onNodeWithText("7.5").assertExists()
    }
}
