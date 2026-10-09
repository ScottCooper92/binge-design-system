package com.binge.designsystem.component

import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val START_INDEX = 20

/** A rotation mid-visit is not a leave: one visit reports once, with its depth (#401). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ScrollDepthRecreationTest {
    @Test
    fun `a recreation reports nothing, and the visit reports once when it ends`() {
        reported.clear()
        val host = Robolectric.buildActivity(DepthHost::class.java).setup()
        idle()

        host.recreate()
        idle()
        assertTrue("reported on recreation: $reported", reported.isEmpty())

        host.pause().stop().destroy()
        idle()
        assertEquals("one report for the visit: $reported", 1, reported.size)
        assertTrue("the depth reached: $reported", (reported.single() ?: -1) >= START_INDEX)
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    /** Hosts a list opened part-way down, so the visit has a depth before any recreation. */
    class DepthHost : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = START_INDEX)
                ScrollDepthEffect(listState) { reported += it }
                LazyColumn(state = listState) {
                    items(List(40) { "row $it" }) { row -> Text(row, Modifier.fillMaxWidth().height(40.dp)) }
                }
            }
        }
    }

    private companion object {
        val reported = mutableListOf<Int?>()
    }
}
