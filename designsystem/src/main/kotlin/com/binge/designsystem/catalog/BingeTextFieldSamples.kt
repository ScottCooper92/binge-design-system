@file:CatalogGroup("Text entry")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTextField
import com.binge.designsystem.preview.ScreenshotTheme

/** A plain field, one with a placeholder, one with a supporting line and one in error: the four states of a field's text. */
@Composable
fun BingeTextFieldSample() {
    var name by remember { mutableStateOf("Living room") }
    var empty by remember { mutableStateOf("") }
    var threads by remember { mutableStateOf("9") }
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            BingeTextField(value = name, onValueChange = { name = it }, label = "Device name", prose = true)
            BingeTextField(value = empty, onValueChange = {
                empty = it
            }, label = "Server address", placeholder = "192.168.1.10:8096", keyboardType = KeyboardType.Uri)
            BingeTextField(value = threads, onValueChange = {
                threads = it
            }, label = "Threads", supporting = "From 1 to 16", keyboardType = KeyboardType.Number)
            BingeTextField(value = threads, onValueChange = {
                threads = it
            }, label = "Threads", supporting = "Enter a number from 1 to 16", isError = true)
        }
    }
}

/** Required, with a leading icon, disabled, read-only and multi-line. */
@Composable
fun BingeTextFieldDecoratedSample() {
    var url by remember { mutableStateOf("https://requests.example.com") }
    var note by remember { mutableStateOf("body { color: red; }") }
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            BingeTextField(value = url, onValueChange = { url = it }, label = "Server URL", required = true, icon = Icons.Filled.Link)
            BingeTextField(value = "Locked", onValueChange = {}, label = "Disabled", enabled = false, icon = Icons.Filled.Link)
            BingeTextField(value = "Read only", onValueChange = {}, label = "Read only", readOnly = true)
            BingeTextField(
                value = note,
                onValueChange = { note = it },
                label = "Custom CSS",
                singleLine = false,
                minLines = 3,
                maxLines = 4,
            )
        }
    }
}

/** A secret, masked, with the eye that reveals it: tap it on a device. */
@Composable
fun BingeTextFieldSecretSample() {
    var key by remember { mutableStateOf("4f9c2a7e1b") }
    ScreenshotTheme {
        BingeTextField(value = key, onValueChange = { key = it }, label = "API key", secret = true)
    }
}

/** A secret whose reveal state the caller owns, here shown: the eye reads "hide". */
@Composable
fun BingeTextFieldRevealedSample() {
    var key by remember { mutableStateOf("4f9c2a7e1b") }
    ScreenshotTheme {
        BingeTextField(value = key, onValueChange = { key = it }, label = "API key", secret = true, revealed = true, onToggleReveal = {})
    }
}

/** The clear button needs focus, so a frame cannot show it: tap the field and type, and an X appears, until it is emptied. */
@Composable
fun BingeTextFieldDemo() {
    var value by remember { mutableStateOf("Tap here, then the X") }
    ScreenshotTheme {
        BingeTextField(value = value, onValueChange = { value = it }, label = "Clearable")
    }
}
