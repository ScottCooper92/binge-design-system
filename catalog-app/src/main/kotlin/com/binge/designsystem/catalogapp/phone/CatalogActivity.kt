package com.binge.designsystem.catalogapp.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.binge.designsystem.theme.BingeExpressiveTheme

/** Opens straight on the list; there is no debug menu in front of it. */
class CatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // The chrome follows the real system theme, never the sample overrides.
            BingeExpressiveTheme {
                CatalogApp()
            }
        }
    }
}
