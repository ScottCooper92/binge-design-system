package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The settings skeleton's first placeholder sits where the settled page's first row does, under the intro, and is the row's size:
 * the same top, width and height, at a font scale of 1 and 2. The settled page is the intro and a group of rows, the blocks spaced
 * as a form body spaces them. Sizes are checked, so the graphics mode is native: legacy mode measures text at one pixel per character.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsPageSkeletonGeometryTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val anchors = listOf(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))

    @Composable
    private fun SettledPage(intro: String?) {
        val inset = resolvedContentInset()
        Column(
            modifier = Modifier.padding(PaddingValues(inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
        ) {
            if (intro != null) PageIntro(intro)
            Column(modifier = Modifier.layoutAnchor(anchors.first())) {
                ItemGroup(title = null, rows = listOf(ListItem(icon = Icons.Filled.Settings, label = "Label", detail = "Detail")))
            }
        }
    }

    private fun check(intro: String?) {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = true) { resolved ->
            BingeExpressiveTheme(dynamicColor = false) {
                if (resolved) {
                    SettledPage(intro)
                } else {
                    SettingsPageSkeleton(contentPadding = PaddingValues(), rows = 4, intro = intro, formInset = true)
                }
            }
        }
    }

    @Test
    fun `the first row is where the settled page puts it`() = check(intro = null)

    @Test
    fun `the first row is where the settled page puts it under an intro`() = check(intro = INTRO)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the first row is where the settled page puts it at a font scale of 2`() = check(intro = null)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the first row is where the settled page puts it under an intro at a font scale of 2`() = check(intro = INTRO)

    private companion object {
        const val INTRO = "Choose how this works. Changes apply as you make them."
    }
}
