package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The people grid's loading state holds the first card where the loaded grid puts it, at the size it gives it, at a font scale of 1, 1.3 and
 * 2, so the grid does not move when its people arrive. Sizes are checked, so the graphics mode is native.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PeopleGridScreenTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val people = List(9) { PeopleGridPerson("p$it", "Person $it", if (it % 2 == 0) "Role $it" else null, photoUrl = null) }

    private fun check(tabs: Int) {
        rule.assertSkeletonReservesGeometry(
            listOf(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
            checkStartEdge = true,
            checkSize = true,
        ) { resolved ->
            val state = if (resolved) PeopleGridState.Ready(listOf(PeopleGridSection("cast", null, people))) else PeopleGridState.Loading
            ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
                PeopleGridScreen(
                    title = "Film",
                    tabs = List(tabs) { PeopleGridTab(if (it == 0) "Cast" else "Crew", state) },
                    selectedTab = 0,
                    onTabChange = {},
                    onOpenPerson = {},
                    onRetry = {},
                )
            }
        }
    }

    @Test
    fun `the skeleton holds the first card with tabs`() = check(tabs = 2)

    @Test
    fun `the skeleton holds the first card with one tab`() = check(tabs = 1)

    @Test
    @Config(fontScale = 1.3f)
    fun `the skeleton holds it at a font scale of 1 point 3`() = check(tabs = 2)

    @Test
    @Config(fontScale = 2f)
    fun `the skeleton holds it at a font scale of 2`() = check(tabs = 2)

    @Test
    fun `each section's people are headed once, and the same person may sit in two sections`() {
        val sections =
            listOf(
                PeopleGridSection("directing", "Directing", people.take(2)),
                PeopleGridSection("writing", "Writing", people.take(1)),
                PeopleGridSection("empty", "Nobody", emptyList()),
            )
        // A heading, then its people; a section with nobody draws nothing, so the bubble's indices stay aligned with the grid's.
        assertEquals(listOf("Directing", "Directing", "Directing", "Writing", "Writing"), sectionTitlesByIndex(sections))
    }
}
