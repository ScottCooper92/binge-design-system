package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LOADING = "Loading"

/** Every phone skeleton, each given the description under test. */
private val skeletons: List<Pair<String, @Composable (String?) -> Unit>> =
    listOf(
        "ListRowSkeleton" to { description -> ListRowSkeleton(description = description) },
        "ListRowSkeletonColumn" to { description -> ListRowSkeletonColumn(PaddingValues(), count = 2, description = description) },
        "CarouselSkeleton" to { description -> CarouselSkeleton(count = 2, description = description) },
        "EpisodeListRowSkeleton" to { description -> EpisodeListRowSkeleton(description = description) },
        "FilterChipRowSkeleton" to { description -> FilterChipRowSkeleton(description = description) },
        "MediaCardSkeleton" to { description -> MediaCardSkeleton(description = description) },
        "WatchProviderGridSkeleton" to { description -> WatchProviderGridSkeleton(description = description) },
    )

/** A loading screen reads as its description, said once; a skeleton given none stays silent, as it always has. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SkeletonDescriptionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var shown by mutableIntStateOf(0)

    private fun showEach(description: String?, check: (name: String) -> Unit) {
        composeTestRule.setContent { TestTheme { skeletons[shown].second(description) } }
        skeletons.indices.forEach { index ->
            shown = index
            composeTestRule.waitForIdle()
            check(skeletons[index].first)
        }
    }

    @Test
    fun `a described skeleton exposes its description once`() {
        showEach(LOADING) { name ->
            val described = composeTestRule.onAllNodesWithContentDescription(LOADING, useUnmergedTree = true).fetchSemanticsNodes()
            assertEquals("$name exposes its description once", 1, described.size)
        }
    }

    @Test
    fun `an undescribed skeleton exposes no description`() {
        showEach(null) { name ->
            val described =
                composeTestRule
                    .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription), useUnmergedTree = true)
                    .fetchSemanticsNodes()
            assertEquals("$name exposes no description", 0, described.size)
        }
    }
}
