package com.binge.designsystem.catalogapp.phone

import androidx.annotation.StringRes
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.overrides.SampleLocale

@StringRes
internal fun SampleLocale.labelRes(): Int =
    when (this) {
        SampleLocale.System -> R.string.locale_system
        SampleLocale.English -> R.string.locale_english
        SampleLocale.Spanish -> R.string.locale_spanish
        SampleLocale.PseudoAccented -> R.string.locale_pseudo_accented
        SampleLocale.PseudoMirrored -> R.string.locale_pseudo_mirrored
    }
