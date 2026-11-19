@file:CatalogGroup("Detail headers")

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailInfoEntry
import com.binge.designsystem.component.DetailInfoGrid
import com.binge.designsystem.component.InfoLink
import com.binge.designsystem.component.InfoValue
import com.binge.designsystem.preview.ScreenshotTheme

private val EnglishFacts = listOf(
    DetailInfoEntry(Icons.Filled.Person, "Director", InfoValue.Link("Christopher Nolan") {}),
    DetailInfoEntry(Icons.Filled.CalendarMonth, "Released", "18 July 2008"),
    DetailInfoEntry(Icons.Filled.Language, "Original language", "English"),
    DetailInfoEntry(
        Icons.Filled.Business,
        "Studios",
        InfoValue.Links(
            listOf(InfoLink("Warner Bros. Pictures") {}, InfoLink("Legendary Pictures") {}, InfoLink("Syncopy") {}),
        ),
    ),
    DetailInfoEntry(Icons.Filled.Info, "Status", "Released"),
    DetailInfoEntry(Icons.Filled.AttachMoney, "Budget", "$185,000,000"),
    DetailInfoEntry(Icons.Filled.AttachMoney, "Revenue", "$1,006,234,167"),
)

private val SpanishFacts = listOf(
    DetailInfoEntry(Icons.Filled.Person, "Dirección", InfoValue.Link("Christopher Nolan") {}),
    DetailInfoEntry(Icons.Filled.CalendarMonth, "Estreno", "18 de julio de 2008"),
    DetailInfoEntry(Icons.Filled.Language, "Idioma original", "Inglés"),
    DetailInfoEntry(
        Icons.Filled.Business,
        "Productoras",
        InfoValue.Links(listOf(InfoLink("Warner Bros. Pictures") {}, InfoLink("Legendary Pictures") {})),
    ),
    DetailInfoEntry(Icons.Filled.Movie, "Estado", "Estrenada"),
    DetailInfoEntry(Icons.Filled.AttachMoney, "Presupuesto", "185.000.000 US$"),
)

/** The facts grid at the end of a detail page: a list on a phone, 2 columns at medium width and 3 at expanded. */
@Composable
fun DetailInfoGridSample() {
    ScreenshotTheme { DetailInfoGrid(entries = EnglishFacts) }
}

/** The same facts in Spanish, whose labels and values run longer. On the tablet frame they wrap inside their column. */
@Composable
fun DetailInfoGridSpanishSample() {
    ScreenshotTheme { DetailInfoGrid(entries = SpanishFacts) }
}
