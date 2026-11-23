@file:CatalogGroup("Sheets")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeFieldsSheetContent
import com.binge.designsystem.component.BingeTextField
import com.binge.designsystem.preview.ScreenshotTheme

@Composable
private fun FieldsSheetSample(
    initialName: String,
    submitting: Boolean = false,
    error: String? = null,
) {
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    ScreenshotTheme {
        BingeFieldsSheetContent(
            title = "Fields sheet",
            body = "Three fields, then submit. Create turns on once the name has text.",
            submitLabel = "Create",
            onSubmit = {},
            onCancel = {},
            submitting = submitting,
            error = error,
            submitEnabled = name.isNotBlank(),
        ) {
            BingeTextField(value = name, onValueChange = { name = it }, label = "Name", required = true)
            BingeTextField(value = address, onValueChange = { address = it }, label = "Address")
            BingeTextField(value = note, onValueChange = { note = it }, label = "Note")
        }
    }
}

/** [BingeFieldsSheetContent] with a name typed: Create is on. Clear the name and it turns off. */
@Composable
fun BingeFieldsSheetSample() = FieldsSheetSample(initialName = "Living room")

/** [BingeFieldsSheetContent] before anything is typed: the name is empty, so Create is off. */
@Composable
fun BingeFieldsSheetEmptySample() = FieldsSheetSample(initialName = "")

/** [BingeFieldsSheetContent] after a submit the server refused: the form keeps its values and the reason shows in red under the fields. */
@Composable
fun BingeFieldsSheetErrorSample() = FieldsSheetSample(initialName = "Living room", error = "That address could not be reached.")

/** [BingeFieldsSheetContent] while its submit is in flight: Create shows its busy state and Cancel is off. */
@Composable
fun BingeFieldsSheetSubmittingSample() = FieldsSheetSample(initialName = "Living room", submitting = true)
