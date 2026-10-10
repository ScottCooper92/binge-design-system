package com.binge.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * [String.uppercase] in the locale the screen is shown in, not [java.util.Locale.ROOT].
 *
 * A heading shown in capitals is translated text, and some languages case it differently from the root
 * rules: in Turkish and Azeri, `i` becomes `İ` and `ı` becomes `I`, so "dizi" reads "DİZİ", not "DIZI".
 * Use it for any visible text the app uppercases; the plain form is for identifiers and never for display.
 */
@Composable
fun String.uppercaseLocalised(): String = uppercase(LocalConfiguration.current.locales[0])
