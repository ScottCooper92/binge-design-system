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
import com.binge.designsystem.component.BingeChoiceSheetContent
import com.binge.designsystem.component.BingeMultiChoiceSheetContent
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
private const val WORLDWIDE = "Worldwide"
private val PINNED_SAMPLE_REGIONS = setOf("AU", "BR", "CA", "FR", "DE", "JP", "ES", "GB", "US")

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
        SheetSurface {
            BingeChoiceSheetContent(title = REGION_TITLE, choices = BingeChoiceList.Ready(SampleRegions), selected = "Japan", onSelect = {})
        }
    }
}

/** A pick that needs context: which copy is being marked under the title, and what the change reaches below it. */
@Composable
fun BingeChoiceSheetSubtitledSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeChoiceSheetContent(
                title = "Mark as",
                subtitle = "4K",
                caption = "Changes the status for everyone on this server.",
                choices = BingeChoiceList.Ready(
                    listOf(
                        BingeChoice("available", "Available"),
                        BingeChoice("partial", "Partially available"),
                        BingeChoice("processing", "Processing"),
                        BingeChoice("unknown", "Unknown"),
                    ),
                ),
                selected = "processing",
                onSelect = {},
            )
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

/**
 * A multi-choice sheet that applies each tick as it is made, for a setting saved as it changes: Clear in the header and
 * no Done, since there is nothing left to confirm.
 */
@Composable
fun BingeMultiChoiceSheetAppliedSample() {
    ScreenshotTheme {
        val chosen = setOf("ja", "es")
        SheetSurface {
            BingeMultiChoiceSheetContent(
                title = LANGUAGES_TITLE,
                choices = BingeChoiceList.Ready(SampleLanguages),
                chosen = chosen,
                onToggle = { _, _ -> },
                clearLabel = CLEAR,
                onClear = {},
            )
        }
    }
}

/** A list read when its sheet opens: loading, then failed with the consumer's message and a retry. */
@Composable
fun BingeChoiceSheetStatesSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            SheetSurface {
                BingeChoiceSheetContent<String>(title = REGION_TITLE, choices = BingeChoiceList.Loading, selected = null, onSelect = {})
            }
            SheetSurface {
                BingeChoiceSheetContent<String>(
                    title = REGION_TITLE,
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

/** The sheet's surface around a [BingeChoiceSheetContent] or [BingeMultiChoiceSheetContent] at rest, which draw none. */
@Composable
private fun SheetSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(DockingSheetShape(0f))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        content = content,
    )
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
        SheetSurface {
            BingeChoiceSheetContent(
                title = REGION_TITLE,
                choices = BingeChoiceList.Ready(SampleRegions.map { it.copy(icon = Icons.Filled.Public) }),
                selected = "Japan",
                suggested = listOf("United Kingdom", "Canada"),
                onSelect = {},
            )
        }
    }
}

private val SampleSpokenLanguages =
    listOf(
        "ar" to "Arabic",
        "zh" to "Chinese",
        "nl" to "Dutch",
        "en" to "English",
        "fr" to "French",
        "de" to "German",
        "hi" to "Hindi",
        "it" to "Italian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "pt" to "Portuguese",
        "es" to "Spanish",
    ).map { (code, name) -> BingeChoice(code, name, mark = code.uppercase()) }

/**
 * A long multi-choice list sectioned: Selected (what was chosen when the sheet opened), Suggested, then All, each row with
 * its language code as a mark and its checkbox at the end. Clear and Done stay in the header.
 */
@Composable
fun BingeMultiChoiceSheetSectionedSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeMultiChoiceSheetContent(
                title = LANGUAGES_TITLE,
                choices = BingeChoiceList.Ready(SampleSpokenLanguages),
                chosen = setOf("ja", "ko"),
                onToggle = { _, _ -> },
                clearLabel = CLEAR,
                onClear = {},
                doneLabel = DONE,
                suggested = listOf("en", "es", "fr"),
            )
        }
    }
}

/**
 * A short multi-choice list whose choices carry marks: each row has its mark first and its checkbox at the end, as a
 * sectioned list's do. "Original language" is pinned, so it leads even the language already chosen.
 */
@Composable
fun BingeMultiChoiceSheetMarkedSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeMultiChoiceSheetContent(
                title = LANGUAGES_TITLE,
                choices =
                    BingeChoiceList.Ready(
                        listOf(BingeChoice("original", "Original language", icon = Icons.Filled.Language)) + SampleSpokenLanguages.take(5),
                    ),
                chosen = setOf("en", "original"),
                onToggle = { _, _ -> },
                clearLabel = CLEAR,
                onClear = {},
                doneLabel = DONE,
                pinned = listOf("original"),
            )
        }
    }
}

/**
 * A pick of people: each row shows the person's avatar where a mark goes, and their initials where there is no image, as
 * a list of users does. A frame cannot load the images, so here every avatar shows its initials.
 */
@Composable
fun BingeMultiChoiceSheetPeopleSample() {
    ScreenshotTheme {
        SheetFrame(docked = false, title = "Requested by", actions = true) {
            MultiChoiceList(
                choices =
                    BingeChoiceList.Ready(
                        listOf(
                            BingeChoice(1, "Ana Lima", avatarName = "Ana Lima", avatarUrl = "https://example.com/ana.png"),
                            BingeChoice(2, "Bo Diaz", avatarName = "Bo Diaz", avatarUrl = "https://example.com/bo.png"),
                            BingeChoice(3, "Cy Ng", avatarName = "Cy Ng"),
                        ),
                    ),
                chosen = setOf(2),
                leading = setOf(2),
                filterPlaceholder = null,
                onToggle = { _, _ -> },
            )
        }
    }
}

/**
 * A sectioned list with a pinned option: "Worldwide" leads with no header, ahead of Current, Suggested and All. It has no
 * flag, so its mark's place is empty, and its label lines up with the flagged regions'.
 */
@Composable
fun BingeChoiceSheetPinnedSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeChoiceSheetContent(
                title = REGION_TITLE,
                choices =
                    BingeChoiceList.Ready(
                        listOf(BingeChoice(WORLDWIDE, WORLDWIDE)) + SampleCountries.filter { it.value in PINNED_SAMPLE_REGIONS },
                    ),
                selected = "JP",
                suggested = listOf("GB"),
                pinned = listOf(WORLDWIDE),
                onSelect = {},
            )
        }
    }
}

/** Marked choices whose labels reach the end of their rows: the text stops the trailing gap short of the radio. */
@Composable
fun BingeChoiceSheetLongLabelSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeChoiceSheetContent(
                title = REGION_TITLE,
                choices =
                    BingeChoiceList.Ready(
                        listOf(
                            BingeChoice("GB", "United Kingdom of Great Britain and Northern Ireland", mark = "GB"),
                            BingeChoice("SH", "Saint Helena, Ascension and Tristan da Cunha", mark = "SH"),
                            BingeChoice("JP", "Japan", mark = "JP"),
                        ),
                    ),
                selected = "SH",
                onSelect = {},
            )
        }
    }
}

/** Choices that carry a count beside the label, as a filter sheet offers: a long label still leaves the count its room. */
@Composable
fun BingeChoiceSheetCountsSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeChoiceSheetContent(
                title = "Images",
                choices =
                    BingeChoiceList.Ready(
                        listOf(
                            BingeChoice("all", "All images", trailingText = "128"),
                            BingeChoice("backdrops", "Backdrops", trailingText = "24"),
                            BingeChoice("posters", "Posters and promotional artwork from every region", trailingText = "1,024"),
                            BingeChoice("logos", "Logos", subtitle = "Transparent PNG", trailingText = "6"),
                        ),
                    ),
                selected = "backdrops",
                onSelect = {},
            )
        }
    }
}

/** The multi-choice sheet's rows with counts, ticked and unticked, with the count at each row's end. */
@Composable
fun BingeMultiChoiceSheetCountsSample() {
    ScreenshotTheme {
        SheetSurface {
            BingeMultiChoiceSheetContent(
                title = "Images",
                choices =
                    BingeChoiceList.Ready(
                        listOf(
                            BingeChoice("backdrops", "Backdrops", trailingText = "24"),
                            BingeChoice("posters", "Posters and promotional artwork from every region", trailingText = "1,024"),
                            BingeChoice("logos", "Logos", trailingText = "6"),
                        ),
                    ),
                chosen = setOf("backdrops"),
                onToggle = { _, _ -> },
                clearLabel = CLEAR,
                onClear = {},
                doneLabel = DONE,
            )
        }
    }
}
