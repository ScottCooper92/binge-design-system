@file:OnePerScreen
@file:CatalogGroup("Sheets")

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeChoice
import com.binge.designsystem.component.BingeChoiceList
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.bingeChoiceItem
import com.binge.designsystem.component.bingeMultiChoiceItem
import com.binge.designsystem.preview.ScreenshotTheme
import kotlinx.coroutines.delay

private const val SLOW_LIST_MILLIS = 1_500L

/**
 * The choice rows opening their real sheets: a short list in a plain sheet, a long one that opens part-way and docks,
 * a multi-choice sheet with its filter, and a list that is read only when its sheet opens, fails the first time and loads on the retry.
 */
@Composable
fun ChoiceSheetsDemo() {
    ScreenshotTheme {
        var region by remember { mutableStateOf<String?>("Japan") }
        var longPick by remember { mutableStateOf<String?>(null) }
        var languages by remember { mutableStateOf(setOf("ja")) }
        var attempts by remember { mutableIntStateOf(0) }
        var slow by remember { mutableStateOf<BingeChoiceList<String>>(BingeChoiceList.Loading) }
        var slowOpened by remember { mutableStateOf(false) }
        LaunchedEffect(attempts, slowOpened) {
            if (!slowOpened) return@LaunchedEffect
            slow = BingeChoiceList.Loading
            delay(SLOW_LIST_MILLIS)
            slow =
                if (attempts == 0) {
                    BingeChoiceList.Failed("The list didn't load.", "Try again") { attempts++ }
                } else {
                    BingeChoiceList.Ready(SampleRegions)
                }
        }
        val many = remember { BingeChoiceList.Ready((1..60).map { BingeChoice("$it", "Choice $it") }) }
        ItemGroup(
            title = "Choice sheets",
            rows =
                listOf(
                    bingeChoiceItem(
                        Icons.Filled.Public,
                        "Short list",
                        BingeChoiceList.Ready(SampleRegions.take(4)),
                        region,
                        "Not set",
                        { region = it },
                    ),
                    bingeChoiceItem(Icons.Filled.Public, "Long list", many, longPick, "Not set", { longPick = it }),
                    bingeMultiChoiceItem(
                        Icons.Filled.Language,
                        "Languages",
                        BingeChoiceList.Ready(SampleLanguages),
                        languages,
                        "Not set",
                        "Done",
                        "Clear",
                        { languages = it },
                        filterPlaceholder = "Filter languages",
                    ),
                    bingeChoiceItem(
                        Icons.Filled.Public,
                        "Slow list",
                        slow,
                        null,
                        "Not set",
                        {},
                        onOpen = { slowOpened = true },
                        selectedLabel = "Japan",
                    ),
                ),
        )
    }
}
