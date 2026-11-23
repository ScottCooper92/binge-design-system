package com.binge.designsystem.tv.focus

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log

private const val TAG = "TvShellHandoff"

/**
 * Notes, in a debug build only, that a focus hand-off ended without the content taking focus: the hub left composition while
 * it still owed its entry a request, say. The hand-offs have no time limit, so a destination that stays on the bar is the
 * failure to look for in logcat under this tag.
 */
fun Context.logTvHandoffMiss(what: String) {
    if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) Log.w(TAG, "Hand-off ended without focus: $what")
}

/**
 * A debug-build trace of the shell's hand-off: when an arrival is armed, cleared or claimed, when a direction key arrives and
 * whether an arrival was pending. Read together under this tag they say which
 * of them moved focus; nothing is logged in a release build.
 */
fun Context.logTvHandoffTrace(what: String) {
    if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) Log.d(TAG, what)
}
