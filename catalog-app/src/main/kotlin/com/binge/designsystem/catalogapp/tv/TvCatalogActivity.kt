package com.binge.designsystem.catalogapp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.binge.designsystem.tv.theme.BingeTvTheme

/** The leanback launcher entry: the TV samples, with the tv-material shell and real D-pad focus. */
class TvCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BingeTvTheme {
                TvCatalogApp()
            }
        }
    }
}
