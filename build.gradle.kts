// Root build file — plugin declarations only, matching Binge's layout. Configuration lives in the
// module that needs it.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.screenshot) apply false
    alias(libs.plugins.android.junit5) apply false
}
