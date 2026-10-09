package com.binge.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A rotation mid-visit is not a leave, so it reports nothing (#401). That a leave reports once, with its depth, is
 * [ScrollDepthEffectTest]'s. This asks only whether the recreation reported at all, so it waits on no layout.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ScrollDepthRecreationTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `a recreation reports nothing`() {
        val reported = mutableListOf<Int?>()
        rule.setContent {
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = 20)
            ScrollDepthEffect(listState) { reported += it }
            LazyColumn(state = listState) {
                items(List(40) { "row $it" }) { row -> Text(row, Modifier.fillMaxWidth().height(40.dp)) }
            }
        }
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        assertTrue("reported on recreation: $reported", reported.isEmpty())
    }
}
