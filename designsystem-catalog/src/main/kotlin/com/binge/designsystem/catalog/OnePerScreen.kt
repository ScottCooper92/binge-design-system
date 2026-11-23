package com.binge.designsystem.catalog

/**
 * Marks a samples file whose component a screen holds one of — a top bar, a navigation bar, a
 * footer, a modal. The catalog app shows such a component one variant at a time, with the variant
 * chosen in its tweaks sheet, instead of stacking every variant in a list: two top bars one above the
 * other is not a layout anything ships. Put it on the file, `@file:OnePerScreen`, above `package`.
 * Everything else, demos included, is listed; a variant that scrolls vertically cannot sit in that
 * list, so its component needs this mark.
 *
 * [fullScreen] goes further: the variant replaces the app's own chrome and runs edge to edge, for a
 * component that is itself the screen's chrome, such as a top bar, which only reads right at the real
 * top of the window. The variant's back action is [LocalDemoBack].
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class OnePerScreen(
    val fullScreen: Boolean = false,
)
