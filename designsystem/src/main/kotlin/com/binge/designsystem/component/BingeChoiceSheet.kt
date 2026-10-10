package com.binge.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import kotlinx.coroutines.launch

/**
 * One entry a choice sheet offers: the [value] a pick hands back, the [label] it shows, and an optional [subtitle]. A
 * choice can carry a mark in a settings row's icon box: an [icon], or a short [mark] of text such as a flag or a
 * language code. A choice that is a person carries [avatarName] instead, drawn as their avatar from [avatarUrl], with
 * their initials while it loads or when there is none. In a list where any choice has a mark, every row keeps the mark's
 * place, empty where it has none, and shows its radio or checkbox last, so the labels line up.
 */
data class BingeChoice<out T>(
    val value: T,
    val label: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val mark: String? = null,
    val avatarName: String? = null,
    val avatarUrl: String? = null,
)

/**
 * From this many choices a list is long: it is sectioned (the chosen, suggestions, then all), and its sheet opens
 * part-way and docks. One rule for both, so a list is never sectioned in a sheet that treats it as short.
 */
internal const val LONG_LIST_THRESHOLD = 8

/** Whether a list of [size] choices is long; see [LONG_LIST_THRESHOLD]. */
internal fun isLongList(size: Int): Boolean = size >= LONG_LIST_THRESHOLD

/** From this many choices the All section gets letter headers and a [BingeLetterRail]. */
internal const val INDEX_THRESHOLD = 40

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
 * Whether a sheet showing this list opens part-way, so the page behind stays in view, and docks at full height. A long
 * list does ([isLongList]); a short one fits a plain sheet. A list still loading is assumed long: short ones rarely load.
 */
internal fun BingeChoiceList<*>.opensPartWay(): Boolean =
    when (this) {
        BingeChoiceList.Loading -> true
        is BingeChoiceList.Failed -> false
        is BingeChoiceList.Ready -> isLongList(choices.size)
    }

/**
 * [opensPartWay] for the list a sheet opened with, kept across a recreation: the restored sheet state was saved under
 * that decision, so recomputing it from a list that has since changed length would give flags that no longer match.
 */
@Composable
internal fun rememberOpensPartWay(choices: BingeChoiceList<*>): Boolean = rememberSaveable { choices.opensPartWay() }

/**
 * A pick of one value from a list. The choices are radio rows, and picking one applies it and closes the sheet.
 *
 * A list of [LONG_LIST_THRESHOLD] or more is sectioned: **Current** (the [selected] choice), **Suggested** ([suggested],
 * in order, where the consumer has a basis: the device's languages, a server's default), then **All**. From
 * [INDEX_THRESHOLD] choices, All has a header per first letter and a [BingeLetterRail] down the edge that jumps to one
 * and follows the scroll. A shorter list reads whole, in its own order.
 *
 * [pinned] values lead the list at any length, in the order given, with no header: an option that stands apart from
 * the rest, such as "Any region". They are left out of the sections below them.
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
    suggested: List<T> = emptyList(),
    pinned: List<T> = emptyList(),
) {
    val partWay = rememberOpensPartWay(choices)
    BingeBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        skipPartiallyExpanded = !partWay,
        dockable = partWay,
        // A long list runs under the navigation bar and pads its own end for it.
        edgeToEdge = partWay,
    ) {
        ChoiceSheetTop(title = title)
        SingleChoiceList(
            choices = choices,
            selected = selected,
            suggested = suggested,
            pinned = pinned,
            underNavigationBar = partWay,
        ) { value ->
            onSelect(value)
            onDismiss()
        }
    }
}

/**
 * A pick of any number of values from a list. The choices are checkboxes. Ticks collect in the sheet and apply only on
 * [doneLabel], which hands back the new set; Clear empties it, and closing the sheet any other way discards the change.
 * With [applyAsPicked], there is no Done: each tick and Clear call [onDone] at once, and closing keeps what was picked.
 * That suits a setting saved as it changes; a filter that re-runs a query per tick keeps Done.
 *
 * A list of [LONG_LIST_THRESHOLD] or more, with no filter text, is sectioned: **Selected** (the [selected] values the
 * sheet opened with, so a row does not jump when it is ticked), **Suggested** ([suggested], in order), then **All**. A
 * value in Suggested and in All is ticked in both. From [INDEX_THRESHOLD] choices, All has a header per first letter and
 * a [BingeLetterRail] down the edge. A shorter list reads whole, with the ones already chosen first. [pinned] values
 * lead at any length, as on [BingeChoiceSheet]. A choice's mark shows on its row whatever the list's length.
 *
 * Pass [filterPlaceholder] for a list long enough to search, a hundred entries or more, say: a filter field then sits
 * under the header and narrows the rows by label. [actions] adds buttons to the header, before Clear and Done. The
 * sheet opens and docks as [BingeChoiceSheet] does.
 *
 * Pass [draftSaver] to keep the ticks across a rotation or process death, as a sheet restored open should. A generic [T]
 * has no saver of its own, so without one the sheet reopens with the ticks reset to [selected]. For `String` values,
 * `Saver(save = { ArrayList(it) }, restore = { it.toSet() })` is enough. With [applyAsPicked] there is no draft to keep.
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
    suggested: List<T> = emptyList(),
    pinned: List<T> = emptyList(),
    actions: @Composable RowScope.() -> Unit = {},
    draftSaver: Saver<Set<T>, out Any>? = null,
    applyAsPicked: Boolean = false,
) {
    val partWay = rememberOpensPartWay(choices)
    // Without a saver, nothing is saved, so the ticks start again from [selected].
    var draft by rememberSaveable(
        stateSaver = draftSaver ?: Saver<Set<T>, Any>(save = {
            null
        }, restore = { null }),
    ) { mutableStateOf(selected) }
    // Pinned at open: with [applyAsPicked], [selected] follows each tick, and a row must not jump under the finger.
    val openedWith = remember { selected }
    val pick: (Set<T>) -> Unit = { picked ->
        draft = picked
        if (applyAsPicked) onDone(picked)
    }
    BingeBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        skipPartiallyExpanded = !partWay,
        dockable = partWay,
        // A long list runs under the navigation bar and pads its own end for it.
        edgeToEdge = partWay,
    ) {
        ChoiceSheetTop(title = title) {
            actions()
            BingeTextButton(label = clearLabel, onClick = { pick(emptySet()) }, enabled = draft.isNotEmpty())
            if (!applyAsPicked) {
                BingeTextButton(
                    label = doneLabel,
                    onClick = {
                        onDone(draft)
                        onDismiss()
                    },
                )
            }
        }
        MultiChoiceList(
            choices = choices,
            chosen = draft,
            leading = openedWith,
            filterPlaceholder = filterPlaceholder,
            onToggle = { value, on -> pick(if (on) draft + value else draft - value) },
            underNavigationBar = partWay,
            suggested = suggested,
            pinned = pinned,
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
 * [draftSaver] keeps the sheet's ticks across a rotation, and [applyAsPicked] applies each tick at once, as on
 * [BingeMultiChoiceSheet].
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
    draftSaver: Saver<Set<T>, out Any>? = null,
    applyAsPicked: Boolean = false,
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
            draftSaver = draftSaver,
            applyAsPicked = applyAsPicked,
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

/**
 * A [BingeChoiceSheet]'s body without its modal window, as the sheet looks at rest: the drag handle and title over the
 * radio rows, sectioned as the sheet sections them. Picking a row calls [onSelect]; nothing closes. Use it to frame a choice sheet in a `@PreviewTest`, where a
 * modal window does not capture, or to show the list inside a sheet of the caller's own. It draws no surface of its own,
 * so it takes the colour of whatever holds it, and its header does not dock. Pass `dragHandle = false` when the
 * sheet holding it draws its own handle, as [BingeBottomSheet] does. It does not clear the navigation bar; a caller
 * in an edge-to-edge sheet does that itself.
 */
@Composable
fun <T> BingeChoiceSheetContent(
    title: String,
    choices: BingeChoiceList<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    suggested: List<T> = emptyList(),
    pinned: List<T> = emptyList(),
    dragHandle: Boolean = true,
) {
    Column(modifier) {
        RestingSheetTop(dragHandle) { ChoiceSheetHeader(title) }
        SingleChoiceList(choices = choices, selected = selected, suggested = suggested, pinned = pinned, onSelect = onSelect)
    }
}

/**
 * A [BingeMultiChoiceSheet]'s body without its modal window, for a `@PreviewTest` frame or a sheet of the caller's own:
 * the title with [clearLabel] (and [doneLabel], when given) at the end, an optional filter, then the checkbox rows ticked
 * where in [chosen]. It holds no draft: each tick calls [onToggle], and Clear and Done call [onClear] and [onDone].
 * [leading] is the selection that comes first, as on the sheet; it defaults to [chosen], which suits a frame. A live
 * caller passes the selection the content opened with, so a row does not jump when ticked. Like
 * [BingeChoiceSheetContent], it draws no surface, its header does not dock, and `dragHandle = false` drops the handle
 * for a sheet that draws its own.
 */
@Composable
fun <T> BingeMultiChoiceSheetContent(
    title: String,
    choices: BingeChoiceList<T>,
    chosen: Set<T>,
    onToggle: (value: T, ticked: Boolean) -> Unit,
    clearLabel: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    doneLabel: String? = null,
    onDone: () -> Unit = {},
    filterPlaceholder: String? = null,
    leading: Set<T> = chosen,
    suggested: List<T> = emptyList(),
    pinned: List<T> = emptyList(),
    actions: @Composable RowScope.() -> Unit = {},
    dragHandle: Boolean = true,
) {
    Column(modifier) {
        RestingSheetTop(dragHandle) {
            ChoiceSheetHeader(title) {
                actions()
                BingeTextButton(label = clearLabel, onClick = onClear, enabled = chosen.isNotEmpty())
                doneLabel?.let { BingeTextButton(label = it, onClick = onDone) }
            }
        }
        MultiChoiceList(
            choices = choices,
            chosen = chosen,
            leading = leading,
            filterPlaceholder = filterPlaceholder,
            onToggle = onToggle,
            suggested = suggested,
            pinned = pinned,
        )
    }
}

/** A choice sheet's top as it rests, undocked: the drag handle over [header]. */
@Composable
private fun RestingSheetTop(showHandle: Boolean, header: @Composable () -> Unit) =
    DockingHeaderLayout(fraction = { 0f }, header = header, dockedTopBar = {}, showHandle = showHandle)

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
    suggested: List<T> = emptyList(),
    pinned: List<T> = emptyList(),
    underNavigationBar: Boolean = false,
    onSelect: (T) -> Unit,
) {
    val markSlot = choices is BingeChoiceList.Ready && choices.choices.any { it.isMarked() }
    if (choices is BingeChoiceList.Ready && isLongList(choices.choices.size)) {
        SectionedChoiceList(
            choices = choices.choices,
            current = listOfNotNull(selected),
            currentLabel = stringResource(R.string.choice_section_current),
            suggested = suggested,
            pinned = pinned,
            underNavigationBar = underNavigationBar,
            modifier = modifier.selectableGroup(),
        ) { choice, clearOfRail -> RadioChoiceRow(choice, choice.value == selected, markSlot, clearOfRail) { onSelect(choice.value) } }
        return
    }
    ChoiceListBody(choices, modifier.selectableGroup(), underNavigationBar) { ready ->
        for (choice in ready.pinnedFirst(pinned)) RadioChoiceRow(choice, choice.value == selected, markSlot) { onSelect(choice.value) }
    }
}

/**
 * [choices] as checkbox rows, ticked where in [chosen]. The values in [leading] come first, so what was already chosen
 * is at the top; [leading] is the selection the sheet opened with, not the live one, so a row does not jump when ticked.
 * With a [filterPlaceholder], a filter field above the rows narrows them by label. [pinned] values come before both. A
 * list with a mark on any choice draws the sectioned list's rows, mark first and checkbox last, at any length.
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
    suggested: List<T> = emptyList(),
    pinned: List<T> = emptyList(),
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
        val markSlot = choices is BingeChoiceList.Ready && choices.choices.any { it.isMarked() }
        if (choices is BingeChoiceList.Ready && isLongList(choices.choices.size) && query.isBlank()) {
            SectionedChoiceList(
                choices = choices.choices,
                current = choices.choices.map { it.value }.filter { it in leading },
                currentLabel = stringResource(R.string.choice_section_selected),
                suggested = suggested,
                pinned = pinned,
                underNavigationBar = underNavigationBar,
                modifier = Modifier,
            ) { choice, clearOfRail ->
                CheckChoiceRow(
                    choice,
                    choice.value in chosen,
                    markSlot,
                    clearOfRail,
                ) { on -> onToggle(choice.value, on) }
            }
            return@Column
        }
        ChoiceListBody(choices, underNavigationBar = underNavigationBar) { ready ->
            val matching = ready.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
            val shown = matching.sortedBy { it.value !in leading }.pinnedFirst(pinned)
            shown.forEachIndexed { index, choice ->
                if (markSlot) {
                    CheckChoiceRow(
                        choice,
                        choice.value in chosen,
                        markSlot = true,
                        clearOfRail = false,
                    ) { on -> onToggle(choice.value, on) }
                } else {
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

/**
 * One radio row: the row owns the selection semantics, so the radio button is a visual indicator only. With [markSlot],
 * the row of a list that has marks, the mark's place leads and the radio ends the row.
 */
@Composable
private fun <T> RadioChoiceRow(
    choice: BingeChoice<T>,
    selected: Boolean,
    markSlot: Boolean,
    clearOfRail: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(
                    start = dimensionResource(R.dimen.padding_m),
                    // A lettered list's rows end short of its rail, so the radio is never under a letter.
                    end = dimensionResource(if (clearOfRail) R.dimen.letter_rail_width else R.dimen.padding_m),
                    top = dimensionResource(R.dimen.padding_s),
                    bottom = dimensionResource(R.dimen.padding_s),
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        if (markSlot) ChoiceMark(choice) else RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = choice.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            choice.subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (markSlot) RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(start = trailingControlExtra()))
    }
}

/** Whether [this] has an [icon][BingeChoice.icon] or a [mark][BingeChoice.mark] to show. */
internal fun BingeChoice<*>.isMarked(): Boolean = icon != null || mark != null || avatarName != null

/**
 * A choice's [icon][BingeChoice.icon] or [mark][BingeChoice.mark] in the box a settings row draws its icon in, or its
 * avatar in the box's place. A choice with none keeps the box's place, empty, so its label lines up with its neighbours'.
 */
@Composable
private fun ChoiceMark(choice: BingeChoice<*>) {
    val avatarName = choice.avatarName
    when {
        // The row's label already names the person, so the avatar's initials stay out of what a screen reader reads.
        avatarName != null ->
            BingeInitialsAvatar(
                name = avatarName,
                avatarUrl = choice.avatarUrl,
                size = dimensionResource(R.dimen.item_group_icon_size),
                modifier = Modifier.clearAndSetSemantics {},
            )
        choice.isMarked() -> ChoiceMarkBox(choice)
        else -> Spacer(Modifier.size(dimensionResource(R.dimen.item_group_icon_size)))
    }
}

@Composable
private fun ChoiceMarkBox(choice: BingeChoice<*>) =
    Box(
        modifier =
            Modifier
                .size(dimensionResource(R.dimen.item_group_icon_size))
                .clip(BingeShapes.MoreCard)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        val icon = choice.icon
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.item_group_icon_glyph)),
            )
        } else {
            Text(choice.mark.orEmpty(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }

/**
 * A long choice list in sections: the [pinned] choices with no header, then the current choices (Current for one,
 * Selected for several), Suggested, then All. From
 * [INDEX_THRESHOLD] choices All is lettered, with sticky headers and a [BingeLetterRail] that jumps to a letter and
 * lights the one at the top of the list. [row] draws one choice, told whether to end short of the rail.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> SectionedChoiceList(
    choices: List<BingeChoice<T>>,
    current: Collection<T>,
    currentLabel: String,
    suggested: List<T>,
    pinned: List<T>,
    underNavigationBar: Boolean,
    modifier: Modifier,
    row: @Composable (BingeChoice<T>, Boolean) -> Unit,
) {
    val sections = remember(choices, current, suggested, pinned) { choiceSections(choices, current, suggested, pinned) }
    val lettered = choices.size >= INDEX_THRESHOLD
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val end = dimensionResource(R.dimen.padding_l)
    val suggestedLabel = stringResource(R.string.choice_section_suggested)
    val allLabel = stringResource(R.string.choice_section_all)
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = end)) {
            sections.pinned.forEach { choice -> item(key = "pinned-${choice.value}") { row(choice, lettered) } }
            choiceSection("current", currentLabel, sections.current, lettered, row)
            choiceSection("suggested", suggestedLabel, sections.suggested, lettered, row)
            if (lettered) {
                sections.byLetter.forEach { (letter, rows) ->
                    stickyHeader(key = "letter-$letter") { ChoiceSectionHeader(letter.toString()) }
                    rows.forEach { choice -> item(key = "all-${choice.value}") { row(choice, true) } }
                }
            } else {
                choiceSection("all", allLabel, sections.all, clearOfRail = false, row)
            }
            // The same inset path as the plain list's: it respects a bar inset already consumed above the sheet.
            if (underNavigationBar) item(key = "navigation-bar") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
        if (lettered) {
            val top by remember(sections) { derivedStateOf { sections.letterAt(listState.firstVisibleItemIndex) } }
            // The rail clears the bar as the list does, so the last letters stay reachable.
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .padding(bottom = end)
                        .then(
                            if (underNavigationBar) {
                                Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                            } else {
                                Modifier
                            },
                        ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                BingeLetterRail(
                    letters = sections.byLetter.map { it.first },
                    onLetter = { letter -> sections.indexOf(letter)?.let { scope.launch { listState.scrollToItem(it) } } },
                    current = top,
                )
            }
        }
    }
}

private fun <T> LazyListScope.choiceSection(
    key: String,
    label: String,
    rows: List<BingeChoice<T>>,
    clearOfRail: Boolean,
    row: @Composable (BingeChoice<T>, Boolean) -> Unit,
) {
    if (rows.isEmpty()) return
    item(key = "header-$key") { ChoiceSectionHeader(label) }
    rows.forEach { choice -> item(key = "$key-${choice.value}") { row(choice, clearOfRail) } }
}

/** One checkbox row of a sectioned or marked list: the mark's place first, with [markSlot], and the checkbox at the end. */
@Composable
private fun <T> CheckChoiceRow(
    choice: BingeChoice<T>,
    checked: Boolean,
    markSlot: Boolean,
    clearOfRail: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onToggle)
                .padding(
                    start = dimensionResource(R.dimen.padding_m),
                    end = dimensionResource(if (clearOfRail) R.dimen.letter_rail_width else R.dimen.padding_m),
                    top = dimensionResource(R.dimen.padding_s),
                    bottom = dimensionResource(R.dimen.padding_s),
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        if (markSlot) ChoiceMark(choice)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = choice.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            choice.subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(start = trailingControlExtra()))
    }
}

/** What a choice row adds to its own gap before a control at its end, to make it [R.dimen.row_trailing_control_gap]. */
@Composable
private fun trailingControlExtra(): Dp = dimensionResource(R.dimen.row_trailing_control_gap) - dimensionResource(R.dimen.padding_m)

@Composable
private fun ChoiceSectionHeader(text: String) =
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(
                    start = dimensionResource(R.dimen.padding_m),
                    end = dimensionResource(R.dimen.padding_m),
                    top = dimensionResource(R.dimen.padding_m),
                    bottom = dimensionResource(R.dimen.padding_xs),
                ),
    )

/**
 * What a sectioned list shows: the pinned choices, the current choice, the suggestions, and [all] the others, also
 * grouped by their first letter.
 */
internal class ChoiceSections<T>(
    val current: List<BingeChoice<T>>,
    val suggested: List<BingeChoice<T>>,
    val byLetter: List<Pair<Char, List<BingeChoice<T>>>>,
    val pinned: List<BingeChoice<T>> = emptyList(),
    val all: List<BingeChoice<T>> = byLetter.flatMap { it.second },
) {
    private val lead =
        pinned.size + (if (current.isEmpty()) 0 else current.size + 1) + (if (suggested.isEmpty()) 0 else suggested.size + 1)

    /** The list index of [letter]'s header. */
    fun indexOf(letter: Char): Int? {
        var index = lead
        for ((l, rows) in byLetter) {
            if (l == letter) return index
            index += rows.size + 1
        }
        return null
    }

    /** The letter whose section holds list item [index]; null while the top is still above the letters. */
    fun letterAt(index: Int): Char? {
        if (index < lead) return null
        var start = lead
        for ((letter, rows) in byLetter) {
            start += rows.size + 1
            if (index < start) return letter
        }
        return byLetter.lastOrNull()?.first
    }
}

/** [label]'s first letter folded to its base letter ("État" is E); anything that is not A–Z is '#'. */
internal fun sectionLetter(label: String): Char {
    val first = label.firstOrNull() ?: return '#'
    val base =
        java.text.Normalizer
            .normalize(first.toString(), java.text.Normalizer.Form.NFD)
            .firstOrNull()
            ?.uppercaseChar()
    return if (base != null && base in 'A'..'Z') base else '#'
}

internal fun <T> choiceSections(
    choices: List<BingeChoice<T>>,
    selected: T?,
    suggested: List<T>,
): ChoiceSections<T> = choiceSections(choices, listOfNotNull(selected), suggested)

/**
 * [choiceSections] for any number of current values: a multi-choice list's chosen ones lead. A [pinned] value is in
 * none of the sections: it leads the list on its own.
 */
internal fun <T> choiceSections(
    choices: List<BingeChoice<T>>,
    current: Collection<T>,
    suggested: List<T>,
    pinned: List<T> = emptyList(),
): ChoiceSections<T> {
    val byValue = choices.associateBy { it.value }
    val rest = choices.filter { it.value !in pinned }
    return ChoiceSections(
        current = current.filter { it !in pinned }.mapNotNull { byValue[it] },
        suggested = suggested.distinct().filter { it !in current && it !in pinned }.mapNotNull { byValue[it] },
        byLetter =
            rest
                .groupBy { sectionLetter(it.label) }
                .toSortedMap()
                .toList(),
        pinned = pinned.distinct().mapNotNull { byValue[it] },
        all = rest,
    )
}

/** These choices with the [pinned] ones first, in the order given; the rest keep their own order. */
internal fun <T> List<BingeChoice<T>>.pinnedFirst(pinned: List<T>): List<BingeChoice<T>> {
    if (pinned.isEmpty()) return this
    val byValue = associateBy { it.value }
    return pinned.distinct().mapNotNull { byValue[it] } + filter { it.value !in pinned }
}
