package com.binge.designsystem.tv.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.LocalTvUserMoves
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.focus.TvUserMoves
import com.binge.designsystem.tv.focus.logTvHandoffMiss
import com.binge.designsystem.tv.focus.logTvHandoffTrace

/** One focus requester per bar item, kept while the set of items is unchanged. */
@Composable
private fun rememberRequesters(slots: TvTopBarSlots): Map<Any, FocusRequester> {
    val all = listOf(slots.header) + slots.items + slots.footer
    return remember(all.map { it.key }) { all.associate { it.key to FocusRequester() } }
}

/**
 * What the user's own moves mean to the shell's arrival: a directional key, or Back from the content to the bar, is the user
 * choosing where focus goes, so an arrival still waiting must not undo it. The arrival itself is claimed by the destination's
 * [com.binge.designsystem.tv.focus.tvShellArrivalTarget].
 */
internal class ShellUserChoices(
    private val userMoves: TvUserMoves,
    val arrival: TvShellArrival? = null,
) {
    /** Records that the user chose the bar, by a directional key or by Back from the content. */
    fun noteChoice() {
        userMoves.note()
        arrival?.clear()
    }

    /** Back on the bar going home: the user has put focus on the home tab, so the switch to it arms no arrival. */
    fun goHome(goHome: () -> Unit) {
        arrival?.quietNextSwitch()
        noteChoice()
        goHome()
    }

    /** Records a directional key press without consuming it. */
    fun noteKey(event: KeyEvent): Boolean {
        if (event.type == KeyEventType.KeyDown && event.key in DIRECTION_KEYS) {
            arrival?.trace("direction key ${event.key}, arrival pending=${arrival.isPending}")
            noteChoice()
        }
        return false
    }
}

/**
 * The user's choices with the shell's arrival armed whenever [destination] changes, and for the first one, which is a cold start or
 * an overlay closing over the shell. The arming is in composition so it precedes the attach of the new destination's nodes; it is
 * idempotent, so a recomposition is harmless.
 */
@Composable
private fun rememberShellUserChoices(destination: Any, userMoves: TvUserMoves): ShellUserChoices {
    val context = LocalContext.current
    val arrival = remember { TvShellArrival(onMiss = { context.logTvHandoffMiss(it) }, onTrace = { context.logTvHandoffTrace(it) }) }
    arrival.armOnChange(destination, armFirst = true)
    return remember { ShellUserChoices(userMoves, arrival) }
}

private val DIRECTION_KEYS = setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)

/** The bar's three slots: the account avatar at the start, the tabs centred, the Settings gear at the end. */
class TvTopBarSlots(
    val header: TvNavItem,
    val items: List<TvNavItem>,
    val footer: TvNavItem,
)

/**
 * The TV shell's navigation as a bar across the top: the [slots]: the account avatar at the
 * start, the tabs centred, a button at the end. A tab commits on click, not on
 * focus, so walking the bar mounts nothing. Down lands in the content; Back in the content lands on the bar, and Back on a
 * tab off [homeKey] goes home. Entering the bar focuses the tab it was last left on.
 *
 * The avatar's menu is [accountRows], built by the app (the menu's rows are the app's own words and destinations); a signed-out
 * header, with no display name, is a plain tab. [accountDetail] is a line under the name, [accountTag] a label beside it (a demo
 * account's "Demo"), [accountExtraContent] a slot under the header (a quota line) and [accountInitiallyOpen] opens the menu for a
 * frame. [pendingKeys] are tabs the host has not yet resolved (which the account gets, from a server): hidden and unfocusable, with the
 * selection kept. All of these default to the behaviour without them. [iconOnlyKeys] are the tabs drawn as an icon alone. [rootModifier] is applied to
 * the scaffold's root after the shell's own key handling, for an app's debug hook. [gearReturnEpoch] moving puts focus on the end
 * button once, for the sheet it opened closing.
 *
 * With [contentBehindBar] the page fills the whole screen and the bar is drawn over it, on a scrim that fades to clear, for pages
 * whose art runs behind the bar. The bar's measured height is provided as [LocalTvTopInset], which the page templates add to their
 * top overscan so a page's own focusable content clears the bar. Focus is as without it: Down from the bar enters the page,
 * Up reaches the bar, and Back climbs as before. Off by default, which draws the page below the bar.
 *
 * Every destination under it claims the focus the shell owes it with [com.binge.designsystem.tv.focus.tvShellArrivalTarget].
 *
 * @need frame a tv screen under a top bar with tabs, with its focus and Back rules
 * @group tv
 * @replaces a hand-built tv top bar scaffold
 */
@Composable
fun TvTopBarScaffold(
    slots: TvTopBarSlots,
    selectedKey: Any,
    homeKey: Any,
    onSelect: (Any) -> Unit,
    contentDepth: Int,
    onGoHome: () -> Unit,
    overlayOpen: Boolean,
    contentFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    accountRows: List<TvAccountMenuRow> = emptyList(),
    accountDetail: String? = null,
    accountTag: String? = null,
    accountExtraContent: (@Composable ColumnScope.() -> Unit)? = null,
    accountInitiallyOpen: Boolean = false,
    iconOnlyKeys: Set<Any> = emptySet(),
    pendingKeys: Set<Any> = emptySet(),
    rootModifier: Modifier = Modifier,
    gearReturnEpoch: Int = 0,
    contentBehindBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    val requesters = rememberRequesters(slots)
    var barHasFocus by remember { mutableStateOf(false) }
    var contentHasFocus by remember { mutableStateOf(false) }
    val userMoves = remember { TvUserMoves() }
    val choices = rememberShellUserChoices(selectedKey to contentDepth, userMoves)

    // True while the avatar's menu is open or its sign-out confirm is up: the bar then holds its tabs at full size, and Back is the
    // menu's to take before the shell's own rule.
    var menuOpen by remember { mutableStateOf(false) }
    // Set to name the bar item that takes focus next, once: the gear after its sheet closes.
    var barEntryOverride by remember { mutableStateOf<Any?>(null) }
    TopBarBackHandler(
        enabled = !overlayOpen && !(barHasFocus && selectedKey == homeKey) && !menuOpen,
        barHasFocus = barHasFocus,
        selectedKey = selectedKey,
        homeKey = homeKey,
        requesters = requesters,
        onGoHome = { choices.goHome(onGoHome) },
        onChoseBar = choices::noteChoice,
    )

    GearReturnEffect(gearReturnEpoch, slots.footer.key) { barEntryOverride = it }
    BarEntryEffect(barEntryOverride, requesters, barHasFocus = { barHasFocus }) { barEntryOverride = null }

    val account = TvTopBarAccount(
        rows = accountRows,
        onOpenChange = { menuOpen = it },
        detail = accountDetail,
        tag = accountTag,
        extraContent = accountExtraContent,
        initiallyOpen = accountInitiallyOpen,
    )

    val bar: @Composable () -> Unit = {
        TvTabTopBar(
            slots = slots,
            selectedKey = selectedKey,
            requesters = requesters,
            down = contentFocusRequester,
            onSelect = onSelect,
            onFocusedChange = { barHasFocus = it },
            holdOpen = menuOpen,
            entryOverride = barEntryOverride,
            account = account,
            iconOnlyKeys = iconOnlyKeys,
            pendingKeys = pendingKeys,
        )
    }
    val contentModifier = Modifier
        .onFocusChanged { contentHasFocus = it.hasFocus }
        .focusRequester(contentFocusRequester)
        .focusGroup()

    Box(
        modifier = modifier
            .fillMaxSize()
            .shellKeys(choices)
            .then(rootModifier),
    ) {
        if (contentBehindBar) {
            ContentBehindBar(bar = bar, contentModifier = contentModifier) {
                CompositionLocalProvider(LocalTvUserMoves provides userMoves, LocalTvShellArrival provides choices.arrival) { content() }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                bar()
                Box(modifier = Modifier.weight(1f).fillMaxWidth().then(contentModifier)) {
                    CompositionLocalProvider(
                        LocalTvUserMoves provides userMoves,
                        LocalTvShellArrival provides choices.arrival,
                    ) { content() }
                }
            }
        }
    }
}

/** The bar's scrim at its top edge, fading to clear at the bottom of the bar, so tab labels read over a bright backdrop. */
private const val BAR_SCRIM_TOP_ALPHA = 0.9f

/**
 * The page across the whole screen with [bar] drawn over its top, on a scrim. The bar is measured first and its height is the
 * page's [LocalTvTopInset] from the page's very first composition, so no frame shows the page under the bar. The bar is subcomposed
 * first, as it is below the page, so focus starts as it does there, and is placed last, so it draws above the page.
 */
@Composable
private fun ContentBehindBar(
    bar: @Composable () -> Unit,
    contentModifier: Modifier,
    content: @Composable () -> Unit,
) {
    val background = MaterialTheme.colorScheme.background
    SubcomposeLayout(Modifier.fillMaxSize().background(background)) { constraints ->
        val bars = subcompose(BarSlot) {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.verticalGradient(listOf(background.copy(alpha = BAR_SCRIM_TOP_ALPHA), Color.Transparent)),
                ),
            ) {
                bar()
            }
        }.map { it.measure(constraints.copy(minWidth = constraints.maxWidth, minHeight = 0)) }
        val inset = (bars.maxOfOrNull { it.height } ?: 0).toDp()
        val pages = subcompose(PageSlot) {
            Box(Modifier.fillMaxSize().then(contentModifier)) {
                CompositionLocalProvider(LocalTvTopInset provides inset, content = content)
            }
        }.map { it.measure(Constraints.fixed(constraints.maxWidth, constraints.maxHeight)) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            pages.forEach { it.place(0, 0) }
            bars.forEach { it.place(0, 0) }
        }
    }
}

private object BarSlot

private object PageSlot

/**
 * Names the gear as the bar's entry each time [epoch] moves, which it does when the Settings sheet the gear opened has closed. A
 * page the sheet opened in between took focus away, and the bar routes entry to its last tab; [BarEntryEffect] makes the request.
 */
@Composable
private fun GearReturnEffect(
    epoch: Int,
    gearKey: Any,
    onEntry: (Any) -> Unit,
) {
    var seen by remember { mutableIntStateOf(epoch) }
    SideEffect {
        if (epoch == seen) return@SideEffect
        seen = epoch
        onEntry(gearKey)
    }
}

/**
 * Puts focus on the bar item [entry] names, once. The bar routes entry by the override it composed
 * with, so the request is made in the effect keyed on it, which runs after the composition that carries it. The override is
 * dropped once the bar has focus; a request that leaves the bar without focus is logged and drops it too.
 */
@Composable
private fun BarEntryEffect(
    entry: Any?,
    requesters: Map<Any, FocusRequester>,
    barHasFocus: () -> Boolean,
    onSpent: () -> Unit,
) {
    val context = LocalContext.current
    val currentBarHasFocus by rememberUpdatedState(barHasFocus)
    val currentOnSpent by rememberUpdatedState(onSpent)
    LaunchedEffect(entry) {
        val key = entry ?: return@LaunchedEffect
        runCatching { requesters[key]?.requestFocus() }
        if (!currentBarHasFocus()) context.logTvHandoffMiss("the bar did not take focus on $key")
        currentOnSpent()
    }
}

/** Back in the content lands on the bar's selected tab, and Back on the bar goes home, taking focus there. */
@Composable
private fun TopBarBackHandler(
    enabled: Boolean,
    barHasFocus: Boolean,
    selectedKey: Any,
    homeKey: Any,
    requesters: Map<Any, FocusRequester>,
    onGoHome: () -> Unit,
    onChoseBar: () -> Unit,
) {
    BackHandler(enabled = enabled) {
        when {
            !barHasFocus -> {
                onChoseBar()
                runCatching { requesters.getValue(selectedKey).requestFocus() }
            }
            else -> {
                onGoHome()
                runCatching { requesters.getValue(homeKey).requestFocus() }
            }
        }
    }
}

/** The scaffold root's key handling: the user's choices. */
@Composable
private fun Modifier.shellKeys(choices: ShellUserChoices): Modifier = onPreviewKeyEvent(choices::noteKey)
