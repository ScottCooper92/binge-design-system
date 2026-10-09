package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R

/** One entry a choice sheet offers: the [value] a pick hands back, the [label] it shows, and an optional [subtitle]. */
data class BingeChoice<out T>(
    val value: T,
    val label: String,
    val subtitle: String? = null,
)

/**
 * The list a choice sheet shows, which may still be on its way. A list read when its sheet opens is [Loading] first;
 * a read that fails is [Failed], with the consumer's own words and a way to try again.
 */
sealed interface BingeChoiceList<out T> {
    data object Loading : BingeChoiceList<Nothing>

    data class Failed(
        val message: String,
        val retryLabel: String,
        val onRetry: () -> Unit,
    ) : BingeChoiceList<Nothing>

    data class Ready<out T>(
        val choices: List<BingeChoice<T>>,
    ) : BingeChoiceList<T>
}

/**
 * Lists longer than this open the sheet part-way, so the page behind stays in view, and let it dock at full height.
 * A shorter list fits a plain sheet, which opens to its own height.
 */
internal const val PEEK_THRESHOLD = 8

/** Whether a sheet showing this list opens part-way. A list still loading is assumed long: short ones rarely load. */
internal fun BingeChoiceList<*>.opensPartWay(): Boolean =
    when (this) {
        BingeChoiceList.Loading -> true
        is BingeChoiceList.Failed -> false
        is BingeChoiceList.Ready -> choices.size > PEEK_THRESHOLD
    }

/**
 * A pick of one value from a list. The choices are radio rows, and picking one applies it and closes the sheet.
 *
 * The sheet scrolls, so a list of any length is reachable. A long list (or one still [loading][BingeChoiceList.Loading])
 * opens the sheet part-way, and dragged up it docks into a [BingeSheetTopBar] with a close button. That is decided
 * once, when the sheet opens, so a list arriving while it is open does not move it.
 */
@Composable
fun <T> BingeChoiceSheet(
    title: String,
    choices: BingeChoiceList<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val partWay = remember { choices.opensPartWay() }
    BingeBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        skipPartiallyExpanded = !partWay,
        dockable = partWay,
        // A long list runs under the navigation bar and pads its own end for it.
        edgeToEdge = partWay,
    ) {
        ChoiceSheetTop(title = title)
        SingleChoiceList(choices = choices, selected = selected, underNavigationBar = partWay) { value ->
            onSelect(value)
            onDismiss()
        }
    }
}

/**
 * A pick of any number of values from a list. The choices are checkboxes, with the ones already chosen first. Ticks
 * collect in the sheet and apply only on [doneLabel], which hands back the new set; Clear empties it, and closing the
 * sheet any other way discards the change.
 *
 * Pass [filterPlaceholder] for a list long enough to search, a hundred entries or more, say: a filter field then sits
 * under the header and narrows the rows by label. The sheet opens and docks as [BingeChoiceSheet] does.
 */
@Composable
fun <T> BingeMultiChoiceSheet(
    title: String,
    choices: BingeChoiceList<T>,
    selected: Set<T>,
    onDone: (Set<T>) -> Unit,
    onDismiss: () -> Unit,
    doneLabel: String,
    clearLabel: String,
    modifier: Modifier = Modifier,
    filterPlaceholder: String? = null,
) {
    val partWay = remember { choices.opensPartWay() }
    // Plain remember: a generic T has no Saver, so a rotation reopens the sheet with the filter kept and the ticks reset.
    var draft by remember { mutableStateOf(selected) }
    BingeBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        skipPartiallyExpanded = !partWay,
        dockable = partWay,
        // A long list runs under the navigation bar and pads its own end for it.
        edgeToEdge = partWay,
    ) {
        ChoiceSheetTop(title = title) {
            BingeTextButton(label = clearLabel, onClick = { draft = emptySet() }, enabled = draft.isNotEmpty())
            BingeTextButton(
                label = doneLabel,
                onClick = {
                    onDone(draft)
                    onDismiss()
                },
            )
        }
        MultiChoiceList(
            choices = choices,
            chosen = draft,
            leading = selected,
            filterPlaceholder = filterPlaceholder,
            onToggle = { value, on -> draft = if (on) draft + value else draft - value },
            underNavigationBar = partWay,
        )
    }
}

/**
 * A setting picked from a list, as a [ListItem] for an [ItemGroup]: its [title], and the chosen value's label as the
 * detail ([emptyLabel] when nothing in the list is chosen). A tap opens a [BingeChoiceSheet]. The sheet belongs to
 * this call, so a screen lists its rows and nothing else. A row that is not [enabled] dims, and closes its sheet if
 * open, so a pick cannot land in a draft that is already being saved.
 *
 * [onOpen] runs when the sheet opens, by a tap or by being restored open after recreation: the point to start reading a list fetched on open. While [choices] is not
 * [BingeChoiceList.Ready] the detail is [selectedLabel], if given, so the row still names the saved value; then
 * [emptyLabel]. The row emits its sheet as it is composed, so build it in composition on each pass, not inside a
 * `remember`.
 */
@Composable
fun <T> bingeChoiceItem(
    icon: ImageVector,
    title: String,
    choices: BingeChoiceList<T>,
    selected: T?,
    emptyLabel: String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
    onOpen: () -> Unit = {},
    selectedLabel: String? = null,
): ListItem {
    var open by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) open = false }
    val currentOnOpen by rememberUpdatedState(onOpen)
    LaunchedEffect(open) { if (open) currentOnOpen() }
    if (open) BingeChoiceSheet(title = title, choices = choices, selected = selected, onSelect = onSelect, onDismiss = { open = false })
    val chosen = (choices as? BingeChoiceList.Ready)?.choices?.firstOrNull { it.value == selected }
    val detail = if (choices is BingeChoiceList.Ready) chosen?.label ?: emptyLabel else selectedLabel ?: emptyLabel
    return ListItem(
        icon = icon,
        label = title,
        detail = detail,
        clickable = enabled,
        disabled = !enabled,
        onClick = { open = true },
    )
}

/**
 * [bingeChoiceItem] for several values: the chosen labels, in list order, as the detail, and a [BingeMultiChoiceSheet]
 * on tap. [onOpen] and [selectedLabel] work as they do there; [selectedLabel] is the whole detail for the selection.
 */
@Composable
fun <T> bingeMultiChoiceItem(
    icon: ImageVector,
    title: String,
    choices: BingeChoiceList<T>,
    selected: Set<T>,
    emptyLabel: String,
    doneLabel: String,
    clearLabel: String,
    onDone: (Set<T>) -> Unit,
    enabled: Boolean = true,
    filterPlaceholder: String? = null,
    onOpen: () -> Unit = {},
    selectedLabel: String? = null,
): ListItem {
    var open by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) open = false }
    val currentOnOpen by rememberUpdatedState(onOpen)
    LaunchedEffect(open) { if (open) currentOnOpen() }
    if (open) {
        BingeMultiChoiceSheet(
            title = title,
            choices = choices,
            selected = selected,
            onDone = onDone,
            onDismiss = { open = false },
            doneLabel = doneLabel,
            clearLabel = clearLabel,
            filterPlaceholder = filterPlaceholder,
        )
    }
    val chosen =
        if (choices is BingeChoiceList.Ready) {
            choices.choices
                .filter { it.value in selected }
                .joinToString { it.label }
        } else {
            selectedLabel.orEmpty()
        }
    return ListItem(
        icon = icon,
        label = title,
        detail = chosen.ifEmpty { emptyLabel },
        clickable = enabled,
        disabled = !enabled,
        onClick = { open = true },
    )
}

/** A choice sheet's top: its title and [actions], docking into a [BingeSheetTopBar] in a sheet that docks. */
@Composable
internal fun ChoiceSheetTop(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    BingeSheetDockingHeader(
        header = { ChoiceSheetHeader(title, actions) },
        dockedTopBar = { onClose -> BingeSheetTopBar(title = title, onClose = onClose, actions = actions) },
    )
}

/** The header a choice sheet shows while it floats: the title, then [actions] at the end. */
@Composable
internal fun ChoiceSheetHeader(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(
                horizontal = dimensionResource(R.dimen.padding_m),
                vertical = dimensionResource(R.dimen.padding_s),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        actions()
    }
}

/** [choices] as radio rows, [selected] marked; or the list's loading or failed body. */
@Composable
internal fun <T> SingleChoiceList(
    choices: BingeChoiceList<T>,
    selected: T?,
    modifier: Modifier = Modifier,
    underNavigationBar: Boolean = false,
    onSelect: (T) -> Unit,
) {
    ChoiceListBody(choices, modifier.selectableGroup(), underNavigationBar) { ready ->
        ready.forEach { choice -> RadioChoiceRow(choice, selected = choice.value == selected) { onSelect(choice.value) } }
    }
}

/**
 * [choices] as checkbox rows, ticked where in [chosen]. The values in [leading] come first, so what was already chosen
 * is at the top; [leading] is the selection the sheet opened with, not the live one, so a row does not jump when ticked.
 * With a [filterPlaceholder], a filter field above the rows narrows them by label.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> MultiChoiceList(
    choices: BingeChoiceList<T>,
    chosen: Set<T>,
    leading: Set<T>,
    filterPlaceholder: String?,
    onToggle: (T, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
    imeVisible: Boolean = WindowInsets.isImeVisible,
    underNavigationBar: Boolean = false,
) {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    // The sheet's keyboard inset pads the bottom of its content, which is off screen while it rests part-way, so the
    // keyboard would rise over the filter. Open the sheet fully while the keyboard is up, as a picker does: the filter
    // then sits at the top with the rows scrolling beneath it. Not on focus: a D-pad or keyboard device focuses the
    // filter as the sheet opens, with no keyboard on screen.
    val dock = LocalBingeSheetDock.current
    val needsRoom = filterPlaceholder != null && imeVisible && dock?.canExpand == true
    LaunchedEffect(needsRoom) { if (needsRoom) dock?.expand() }
    Column(modifier) {
        if (filterPlaceholder != null && choices is BingeChoiceList.Ready) {
            BingeSearchField(
                query = query,
                onQueryChange = { query = it },
                onClear = { query = "" },
                placeholder = filterPlaceholder,
                // The field's own default is the sheet's colour, which would leave it invisible here.
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier =
                    Modifier.padding(
                        horizontal = dimensionResource(R.dimen.padding_m),
                        vertical = dimensionResource(R.dimen.padding_s),
                    ),
            )
        }
        ChoiceListBody(choices, underNavigationBar = underNavigationBar) { ready ->
            val matching = ready.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
            val shown = matching.sortedBy { it.value !in leading }
            shown.forEachIndexed { index, choice ->
                CheckboxRow(
                    label = choice.label,
                    subtitle = choice.subtitle,
                    checked = choice.value in chosen,
                    onToggle = { on -> onToggle(choice.value, on) },
                    showDivider = index < shown.lastIndex,
                )
            }
        }
    }
}

/**
 * A choice list's body: the rows for a ready list, scrolling on their own so the last of hundreds is reachable at any
 * sheet height; a loading indicator; or the failure's message with its retry. [underNavigationBar] is for an
 * edge-to-edge sheet: each of the three then also clears the navigation bar it runs under.
 */
@Composable
private fun <T> ChoiceListBody(
    choices: BingeChoiceList<T>,
    modifier: Modifier = Modifier,
    underNavigationBar: Boolean = false,
    rows: @Composable ColumnScope.(List<BingeChoice<T>>) -> Unit,
) {
    val inset = dimensionResource(R.dimen.padding_l)
    val clearBar =
        if (underNavigationBar) {
            Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
        } else {
            Modifier
        }
    when (choices) {
        BingeChoiceList.Loading -> {
            // In a docking sheet the wait is a window tall, as the list will be. A sheet holding only a spinner fits
            // whole, so it would settle fully open and then fill the screen when the list arrived; this tall, it peeks
            // part-way and the list arrives into a sheet that stays where it is.
            val windowHeight = with(LocalDensity.current) {
                LocalWindowInfo.current.containerSize.height
                    .toDp()
            }
            val tall = if (LocalBingeSheetDock.current != null) Modifier.heightIn(min = windowHeight) else Modifier
            Box(
                modifier
                    .fillMaxWidth()
                    .then(clearBar)
                    .then(tall)
                    .padding(inset),
                contentAlignment = Alignment.TopCenter,
            ) { BingeLoadingIndicator() }
        }
        is BingeChoiceList.Failed ->
            Column(
                modifier = modifier.fillMaxWidth().then(clearBar).padding(inset),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                Text(
                    choices.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                BingeTextButton(label = choices.retryLabel, onClick = choices.onRetry)
            }
        is BingeChoiceList.Ready ->
            Column(modifier.verticalScroll(rememberScrollState()).padding(bottom = inset)) {
                rows(choices.choices)
                if (underNavigationBar) Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
    }
}

/** One radio row: the row owns the selection semantics, so the radio button is a visual indicator only. */
@Composable
private fun <T> RadioChoiceRow(
    choice: BingeChoice<T>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = dimensionResource(R.dimen.padding_m), vertical = dimensionResource(R.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = choice.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            choice.subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
