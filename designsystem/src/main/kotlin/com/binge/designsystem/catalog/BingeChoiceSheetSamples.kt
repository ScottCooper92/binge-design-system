@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeChoice
import com.binge.designsystem.component.BingeChoiceList
import com.binge.designsystem.component.BingeSheetTopBar
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.ChoiceSheetHeader
import com.binge.designsystem.component.DockingHeaderLayout
import com.binge.designsystem.component.DockingSheetShape
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.MultiChoiceList
import com.binge.designsystem.component.SingleChoiceList
import com.binge.designsystem.component.bingeChoiceItem
import com.binge.designsystem.component.bingeMultiChoiceItem
import com.binge.designsystem.preview.ScreenshotTheme

private const val REGION_TITLE = "Region"
private const val LANGUAGES_TITLE = "Languages"
private const val DONE = "Done"
private const val CLEAR = "Clear"
private const val FILTER = "Filter languages"
private const val NOT_SET = "Not set"

internal val SampleRegions =
    listOf("Australia", "Brazil", "Canada", "France", "Germany", "Japan", "Spain", "United Kingdom", "United States")
        .map { BingeChoice(it, it) }

internal val SampleLanguages =
    listOf(
        BingeChoice("de", "German", "Deutsch"),
        BingeChoice("en", "English"),
        BingeChoice("es", "Spanish", "Español"),
        BingeChoice("fr", "French", "Français"),
        BingeChoice("ja", "Japanese", "日本語"),
        BingeChoice("pt", "Portuguese", "Português"),
    )

/** A region's flag from its two-letter code, for the samples' marks. */
private fun flag(code: String): String = code.map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")

private val SampleCountryCodes =
    listOf(
        "AR",
        "AU",
        "AT",
        "BE",
        "BR",
        "BG",
        "CA",
        "CL",
        "CN",
        "CO",
        "HR",
        "CZ",
        "DK",
        "EG",
        "FI",
        "FR",
        "DE",
        "GR",
        "HK",
        "HU",
        "IS",
        "IN",
        "ID",
        "IE",
        "IL",
        "IT",
        "JP",
        "KE",
        "MX",
        "NL",
        "NZ",
        "NG",
        "NO",
        "PE",
        "PH",
        "PL",
        "PT",
        "RO",
        "SA",
        "SG",
        "ZA",
        "KR",
        "ES",
        "SE",
        "CH",
        "TW",
        "TH",
        "TR",
        "GB",
        "US",
    )

/** Fifty regions, each with its flag as a mark: long enough for letter headers and the rail. */
internal val SampleCountries =
    SampleCountryCodes
        .map { code ->
            BingeChoice(
                code,
                java.util.Locale
                    .Builder()
                    .setRegion(code)
                    .build()
                    .getDisplayCountry(java.util.Locale.UK),
                mark = flag(code),
            )
        }.sortedBy { it.label }

/** A single-choice sheet as it opens part-way: the title over radio rows, the current pick marked. */
@Composable
fun BingeChoiceSheetSample() {
    ScreenshotTheme {
        SheetFrame(docked = false, title = REGION_TITLE) {
            SingleChoiceList(choices = BingeChoiceList.Ready(SampleRegions), selected = "Japan", onSelect = {})
        }
    }
}

/** The same sheet dragged to full height: its header has docked into a top bar with a close button. */
@Composable
fun BingeChoiceSheetDockedSample() {
    ScreenshotTheme {
        SheetFrame(docked = true, title = REGION_TITLE) {
            SingleChoiceList(choices = BingeChoiceList.Ready(SampleRegions), selected = "Japan", onSelect = {})
        }
    }
}

/**
 * A multi-choice sheet with its filter in use: Clear and Done in the header, the rows narrowed to those matching "an",
 * and what was already chosen first.
 */
@Composable
fun BingeMultiChoiceSheetSample() {
    ScreenshotTheme {
        val chosen = setOf("ja", "es")
        SheetFrame(docked = false, title = LANGUAGES_TITLE, actions = true) {
            MultiChoiceList(
                choices = BingeChoiceList.Ready(SampleLanguages),
                chosen = chosen,
                leading = chosen,
                filterPlaceholder = FILTER,
                onToggle = { _, _ -> },
                initialQuery = "an",
            )
        }
    }
}

/** A list read when its sheet opens: loading, then failed with the consumer's message and a retry. */
@Composable
fun BingeChoiceSheetStatesSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            SheetFrame(docked = false, title = REGION_TITLE) {
                SingleChoiceList<String>(choices = BingeChoiceList.Loading, selected = null, onSelect = {})
            }
            SheetFrame(docked = false, title = REGION_TITLE) {
                SingleChoiceList<String>(
                    choices = BingeChoiceList.Failed("The server didn't send its regions.", "Try again") {},
                    selected = null,
                    onSelect = {},
                )
            }
        }
    }
}

/** The rows that open these sheets, in a group: one value, several values, and one with nothing chosen. */
@Composable
fun BingeChoiceItemSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Discover",
            rows =
                listOf(
                    bingeChoiceItem(Icons.Filled.Public, REGION_TITLE, BingeChoiceList.Ready(SampleRegions), "Japan", NOT_SET, {}),
                    bingeMultiChoiceItem(
                        Icons.Filled.Language,
                        LANGUAGES_TITLE,
                        BingeChoiceList.Ready(SampleLanguages),
                        setOf("es", "ja"),
                        NOT_SET,
                        DONE,
                        CLEAR,
                        {},
                    ),
                    bingeChoiceItem(Icons.Filled.Public, "Streaming region", BingeChoiceList.Ready(SampleRegions), null, NOT_SET, {}),
                ),
        )
    }
}

/**
 * A choice sheet's top and body at rest, floating or [docked], drawn directly: `BingeBottomSheet` is a modal window
 * that a frame does not capture. [actions] adds the multi-choice sheet's Clear and Done.
 */
@Composable
private fun SheetFrame(
    docked: Boolean,
    title: String,
    actions: Boolean = false,
    body: @Composable ColumnScope.() -> Unit,
) {
    val fraction = if (docked) 1f else 0f
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(DockingSheetShape(fraction))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        DockingHeaderLayout(
            fraction = { fraction },
            header = { ChoiceSheetHeader(title) { if (actions) SampleActions() } },
            dockedTopBar = { BingeSheetTopBar(title = title, onClose = {}, actions = { if (actions) SampleActions() }) },
        )
        body()
    }
}

@Composable
private fun SampleActions() {
    BingeTextButton(label = CLEAR, onClick = {})
    BingeTextButton(label = DONE, onClick = {})
}

/**
 * A long list sectioned: Current, then Suggested (here the device's region and two popular ones), then All under a
 * letter per header with the rail down the edge. Each region carries its flag as a mark, its radio at the row's end.
 */
@Composable
fun BingeChoiceSheetLetteredSample() {
    ScreenshotTheme {
        SheetFrame(docked = true, title = REGION_TITLE) {
            SingleChoiceList(
                choices = BingeChoiceList.Ready(SampleCountries),
                selected = "JP",
                suggested = listOf("GB", "US", "CA"),
                onSelect = {},
            )
        }
    }
}

/** A shorter list, sectioned without letters: Current, Suggested, then All in the list's own order, with icons. */
@Composable
fun BingeChoiceSheetSectionedSample() {
    ScreenshotTheme {
        SheetFrame(docked = false, title = REGION_TITLE) {
            SingleChoiceList(
                choices = BingeChoiceList.Ready(SampleRegions.map { it.copy(icon = Icons.Filled.Public) }),
                selected = "Japan",
                suggested = listOf("United Kingdom", "Canada"),
                onSelect = {},
            )
        }
    }
}
