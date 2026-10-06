package com.binge.designsystem.component

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * The [Activity] under this context, unwrapping any [ContextWrapper]s, or null when there is none. A
 * `Dialog`'s content and a host-wrapped context reach the Activity only this way; a cast throws there.
 */
internal tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
