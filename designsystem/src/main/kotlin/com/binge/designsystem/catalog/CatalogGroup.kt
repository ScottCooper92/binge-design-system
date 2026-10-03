package com.binge.designsystem.catalog

/**
 * Lists every sample and demo in a file under one named component in the catalog app, instead of the
 * component its file name gives. It gathers components a screen chooses between, such as the top bars,
 * onto one page whose sheet switches between them. Put it on the file, `@file:CatalogGroup("…")`,
 * above `package`; every file that names the same group shares the page.
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class CatalogGroup(
    val name: String,
)

/**
 * Keeps a file's samples out of the catalog app: they stay the screenshot suite's fixtures, and a live
 * demo stands in for them on a device. Use it where a still frame of a scroll-driven component is no
 * way to judge it, such as a top bar at one scroll offset. Put it on the file, `@file:ScreenshotOnly`.
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class ScreenshotOnly
