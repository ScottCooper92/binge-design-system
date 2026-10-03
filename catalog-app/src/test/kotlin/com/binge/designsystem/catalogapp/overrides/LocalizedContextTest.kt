package com.binge.designsystem.catalogapp.overrides

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.binge.designsystem.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.Locale

/**
 * A locale override hands the sample a context whose strings are in that locale, and which still
 * unwraps to the activity. A bare configuration context would lose the activity, and with it the fold
 * `FoldPosture` reads through `WindowInfoTracker` (#215).
 */
@RunWith(RobolectricTestRunner::class)
class LocalizedContextTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    @Test
    fun `the localized context reads strings in its locale`() {
        val localized = activity.localizedTo(Locale.forLanguageTag("es"))

        assertEquals(
            "es",
            localized.resources.configuration.locales[0]
                .language,
        )
        assertEquals("Nueva lista", localized.getString(R.string.create_list_title))
    }

    @Test
    fun `the localized context still unwraps to the activity`() {
        val localized = activity.localizedTo(Locale.forLanguageTag("es"))

        assertSame(activity, localized.unwrapToActivity())
    }

    private fun Context.unwrapToActivity(): Activity? {
        var context: Context? = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
