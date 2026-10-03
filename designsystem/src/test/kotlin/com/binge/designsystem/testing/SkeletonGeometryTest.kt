package com.binge.designsystem.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The harness consumers run against their skeletons, run against itself: it passes a skeleton that holds
 * its anchors still and fails each way the KDoc says a skeleton can drift.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SkeletonGeometryTest {
    @get:Rule
    val rule = createComposeRule()

    private val anchor = LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW)

    @Test
    fun `a skeleton that holds its anchor still passes`() {
        rule.assertSkeletonReservesGeometry(listOf(anchor)) { _ ->
            Box(Modifier.size(40.dp).layoutAnchor(anchor))
        }
    }

    @Test
    fun `an anchor that moves down on resolve fails`() {
        assertThrows(IllegalStateException::class.java) {
            rule.assertSkeletonReservesGeometry(listOf(anchor)) { resolved ->
                Column {
                    if (resolved) Spacer(Modifier.height(50.dp))
                    Box(Modifier.size(40.dp).layoutAnchor(anchor))
                }
            }
        }
    }

    @Test
    fun `an anchor only one side carries fails`() {
        assertThrows(IllegalStateException::class.java) {
            rule.assertSkeletonReservesGeometry(listOf(anchor)) { resolved ->
                Box(Modifier.size(40.dp).then(if (resolved) Modifier else Modifier.layoutAnchor(anchor)))
            }
        }
    }

    @Test
    fun `a resize in place passes by default`() {
        rule.assertSkeletonReservesGeometry(listOf(anchor)) { resolved ->
            Box(Modifier.size(if (resolved) 80.dp else 40.dp).layoutAnchor(anchor))
        }
    }

    @Test
    fun `a resize in place fails with checkSize`() {
        assertThrows(IllegalStateException::class.java) {
            rule.assertSkeletonReservesGeometry(listOf(anchor), checkSize = true) { resolved ->
                Box(Modifier.size(if (resolved) 80.dp else 40.dp).layoutAnchor(anchor))
            }
        }
    }

    @Test
    fun `naming no anchors is refused rather than passing vacuously`() {
        assertThrows(IllegalArgumentException::class.java) {
            rule.assertSkeletonReservesGeometry(emptyList()) { _ -> Box(Modifier.size(40.dp)) }
        }
    }
}
