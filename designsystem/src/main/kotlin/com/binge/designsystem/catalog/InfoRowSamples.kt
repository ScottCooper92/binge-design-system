package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.InfoLink
import com.binge.designsystem.component.InfoRow
import com.binge.designsystem.component.InfoRowEntry
import com.binge.designsystem.component.InfoRowList
import com.binge.designsystem.component.InfoValue
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [InfoRow] / [InfoRowList] — key-value detail rows: a single plain row, a stacked
 * list, and the linkable variants (single link + a wrapping list of links). See the convention KDoc
 * on [MediaCardRatedSample].
 */
@Composable
fun InfoRowSingleSample() {
    ScreenshotTheme {
        InfoRow(label = "Director", value = "Christopher Nolan")
    }
}

/** A stack of plain key-value rows — labels share a fixed left column so values align. */
@Composable
fun InfoRowListSample() {
    ScreenshotTheme {
        InfoRowList(
            entries = listOf(
                InfoRowEntry("Director", "Christopher Nolan"),
                InfoRowEntry("Released", "2008"),
                InfoRowEntry("Original language", "English"),
            ),
        )
    }
}

/** The linkable variants — a single [InfoValue.Link] and a wrapping [InfoValue.Links] alongside plain text. */
@Composable
fun InfoRowLinkedSample() {
    ScreenshotTheme {
        InfoRowList(
            entries = listOf(
                InfoRowEntry("Director", InfoValue.Link("Christopher Nolan") {}),
                InfoRowEntry(
                    "Studios",
                    InfoValue.Links(
                        listOf(
                            InfoLink("Warner Bros.") {},
                            InfoLink("Legendary") {},
                            InfoLink("Syncopy") {},
                        ),
                    ),
                ),
                InfoRowEntry("Released", "2008"),
            ),
        )
    }
}
