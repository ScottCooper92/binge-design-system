@file:CatalogGroup("Text entry")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.component.TvSearchField
import com.binge.designsystem.tv.component.TvTextField
import com.binge.designsystem.R as DesR

/** A search bar's share of a panel beside a mic, narrow enough that a long placeholder has to give way. */
private const val NARROW_SEARCH_FRACTION = 0.42f

/** A form's fields in each state: empty with a placeholder, focused, filled, secret, and disabled. */
@Composable
fun TvTextFieldStatesSample() {
    var address by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("Living room") }
    var user by remember { mutableStateOf("scott") }
    var password by remember { mutableStateOf("hunter22") }
    var port by remember { mutableStateOf("5055") }
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        TvTextField(value = address, onValueChange = { address = it }, label = "Server address", placeholder = "http://192.168.1.10:5055")
        TvTextField(value = name, onValueChange = { name = it }, label = "Display name", initiallyFocused = true)
        TvTextField(value = user, onValueChange = { user = it }, label = "Username", autoCorrect = false)
        TvTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            secret = true,
            keyboardType = KeyboardType.Password,
        )
        TvTextField(value = port, onValueChange = { port = it }, label = "Port", enabled = false)
    }
}

/** The search bar resting with its placeholder, and focused with a query typed. */
@Composable
fun TvSearchFieldStatesSample() {
    var empty by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("Dune") }
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        TvSearchField(
            value = empty,
            onValueChange = { empty = it },
            label = "Search",
            placeholder = "Search films, shows or people",
            onSearch = {},
        )
        TvSearchField(
            value = query,
            onValueChange = { query = it },
            label = "Search",
            placeholder = "Search films, shows or people",
            onSearch = {},
            initiallyFocused = true,
        )
    }
}

/**
 * A narrow search bar beside a mic, with copy longer than it: a Spanish placeholder, then a long typed query. Both end
 * in an ellipsis on one line rather than clipping mid-word.
 */
@Composable
fun TvSearchFieldNarrowSample() {
    var empty by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("El señor de los anillos: la comunidad del anillo") }
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        NarrowSearchRow(value = empty, onValueChange = { empty = it })
        NarrowSearchRow(value = query, onValueChange = { query = it })
    }
}

@Composable
private fun NarrowSearchRow(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(NARROW_SEARCH_FRACTION),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_sm)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvSearchField(
            value = value,
            onValueChange = onValueChange,
            label = "Buscar",
            placeholder = "Busca películas, series o personas",
            onSearch = {},
            modifier = Modifier.weight(1f),
        )
        TvIconButton(icon = Icons.Filled.Mic, label = "Buscar por voz", onClick = {})
    }
}
