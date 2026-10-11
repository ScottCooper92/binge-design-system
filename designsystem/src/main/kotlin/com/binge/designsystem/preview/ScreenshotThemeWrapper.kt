package com.binge.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.binge.designsystem.theme.BingeBrand

/**
 * Applies [ScreenshotTheme] to every cell of the multipreview that carries it.
 *
 * Open so an app with its own [BingeBrand] reuses the theme body rather than copying it. It subclasses this and
 * overrides [brand], then binds the subclass on annotations of its own, built from the public specs in
 * `DevicePreviews.kt`:
 *
 * ```
 * class MyScreenshotThemeWrapper : ScreenshotThemeWrapper() {
 *     override val brand = MyBrand
 * }
 * ```
 *
 * The brand is a property rather than a parameter because a `PreviewWrapperProvider` is built with no arguments.
 */
open class ScreenshotThemeWrapper : PreviewWrapperProvider {
    /** The brand every cell renders in. Binge's by default. */
    open val brand: BingeBrand get() = BingeBrand.Binge

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        ScreenshotTheme(brand = brand) {
            content()
        }
    }
}
