package com.binge.designsystem.component

import android.app.Activity
import android.view.ContextThemeWrapper
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FindActivityTest {
    private val activity: Activity = Robolectric.buildActivity(Activity::class.java).get()

    @Test
    fun anActivityIsItsOwnActivity() {
        assertSame(activity, activity.findActivity())
    }

    @Test
    fun aDialogStyleWrapperUnwrapsToTheActivity() {
        val wrapped = ContextThemeWrapper(ContextThemeWrapper(activity, 0), 0)
        assertSame(activity, wrapped.findActivity())
    }

    @Test
    fun theApplicationHasNoActivity() {
        assertNull(RuntimeEnvironment.getApplication().findActivity())
    }
}
