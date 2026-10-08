@file:CatalogGroup("Text entry")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingePinField
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [BingePinField]: two digits of a four-digit PIN typed, the third box next. Typing finishes it, and
 * backspace walks back through the boxes.
 */
@Composable
fun BingePinFieldSample() {
    var pin by remember { mutableStateOf("48") }
    ScreenshotTheme {
        BingePinField(value = pin, onValueChange = { pin = it }, label = "PIN shown on the TV", autoFocus = false)
    }
}

/** Public sample for [BingePinField] after a wrong PIN: every box is marked, and backspace edits the code and clears the mark. */
@Composable
fun BingePinFieldErrorSample() {
    var pin by remember { mutableStateOf("4821") }
    var wrong by remember { mutableStateOf(true) }
    ScreenshotTheme {
        BingePinField(
            value = pin,
            onValueChange = {
                pin = it
                wrong = false
            },
            label = "PIN shown on the TV",
            error = "That PIN doesn't match the TV".takeIf { wrong },
            autoFocus = false,
        )
    }
}
