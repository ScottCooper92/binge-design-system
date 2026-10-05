package com.binge.designsystem.template

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeShapes

/** A form's commit or its way out: what the button says, whether it is live, and whether it is working. */
@Immutable
data class FormAction(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val busy: Boolean = false,
)

/** Where a [FormScreen]'s [FormAction]s sit. */
enum class FormActionPlacement {
    /** The primary action as a text button in the top bar: a short form that saves in place. */
    TopBar,

    /** A bar pinned under the form, with the secondary action beside the primary: a long form, or a submit. */
    Footer,
}

/**
 * A form or an editor: the fields in a scrolling column under [BingeScreenScaffold]'s bar, lifted by the
 * keyboard rather than covered by it, with its commit in the bar or pinned under the form.
 *
 * Until [notReady] is null the screen shows it instead (the record loading, or its load failing). [scrolling]
 * is off for a body that scrolls itself; it then folds the padding it is handed into its own list. Tag fields
 * with [formField] and a failed submit can bring the first problem into view through [FormFields].
 */
@Composable
fun FormScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    primaryAction: FormAction? = null,
    placement: FormActionPlacement = FormActionPlacement.TopBar,
    secondaryAction: FormAction? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrolling: Boolean = true,
    extraActions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    notReady: (@Composable (padding: PaddingValues) -> Unit)? = null,
    content: @Composable ColumnScope.(padding: PaddingValues) -> Unit,
) {
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = onBack,
        bar = ScreenBar.Small,
        snackbarHostState = snackbarHostState,
        actions = {
            if (placement == FormActionPlacement.TopBar && primaryAction != null && notReady == null) {
                BingeTextButton(
                    label = primaryAction.label,
                    onClick = primaryAction.onClick,
                    enabled = primaryAction.enabled,
                    loading = primaryAction.busy,
                )
            }
            extraActions()
        },
        bottomBar = {
            bottomBar()
            if (placement == FormActionPlacement.Footer && primaryAction != null && notReady == null) {
                FormFooter(primary = primaryAction, secondary = secondaryAction)
            }
        },
    ) { padding ->
        // The bars' insets are consumed first, so the navigation bar under the keyboard is not counted twice.
        Box(modifier = Modifier.fillMaxSize().padding(padding.screenOuterPadding()).consumeWindowInsets(padding)) {
            val inner = padding.screenInnerPadding()
            if (notReady != null) {
                notReady(inner)
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .imePadding()
                            .then(if (scrolling) Modifier.verticalScroll(rememberScrollState()).padding(inner) else Modifier)
                            .padding(resolvedContentInset()),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
                ) {
                    content(if (scrolling) PaddingValues() else inner)
                }
            }
        }
    }
}

/**
 * One titled group of a form's fields, laid out like `ItemGroup`: the title above the surface, the fields on
 * it. The title is a heading, so a screen reader can jump group to group.
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_s))
                    .padding(bottom = dimensionResource(R.dimen.padding_s))
                    .semantics { heading() },
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(BingeShapes.Large)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
            content = content,
        )
    }
}

/**
 * The bar pinned under a [FormScreen] in [FormActionPlacement.Footer]: the secondary action beside the
 * primary, both full-width halves, or the primary alone across the bar.
 */
@Composable
fun FormFooter(
    primary: FormAction,
    modifier: Modifier = Modifier,
    secondary: FormAction? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = BingeShapes.HeroTop,
        shadowElevation = dimensionResource(R.dimen.snackbar_elevation),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = resolvedContentInset())
                    .padding(top = dimensionResource(R.dimen.padding_sm), bottom = dimensionResource(R.dimen.padding_m)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            secondary?.let {
                BingeOutlinedButton(
                    label = it.label,
                    onClick = it.onClick,
                    enabled = it.enabled,
                    loading = it.busy,
                    modifier = Modifier.weight(1f),
                )
            }
            BingeFilledButton(
                label = primary.label,
                onClick = primary.onClick,
                enabled = primary.enabled,
                loading = primary.busy,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * The fields of one form a failed submit can take the user to. Tag each with [formField]; [bringIntoView]
 * scrolls the named field into view and, for one that can take it, moves focus there.
 */
@OptIn(ExperimentalFoundationApi::class)
@Stable
class FormFields {
    private val fields = mutableStateMapOf<String, FormFieldHandle>()

    /** Brings field [id] into view, or [fallback] (its section's header, say) when [id] is not on screen. */
    suspend fun bringIntoView(id: String, fallback: String? = null): Boolean {
        val handle = fields[id] ?: fallback?.let { fields[it] } ?: return false
        handle.bringIntoView.bringIntoView()
        handle.focus?.requestFocus()
        return true
    }

    internal fun register(id: String, handle: FormFieldHandle) {
        fields[id] = handle
    }

    internal fun unregister(id: String, handle: FormFieldHandle) {
        if (fields[id] === handle) fields.remove(id)
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal class FormFieldHandle(
    val bringIntoView: BringIntoViewRequester,
    val focus: FocusRequester?,
)

/** The [FormFields] for one form, stable across recomposition. */
@Composable
fun rememberFormFields(): FormFields = remember { FormFields() }

/** Tags this field as [id] in [fields]. [takesFocus] is for a text field; a picker row is only scrolled to. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.formField(
    fields: FormFields,
    id: String,
    takesFocus: Boolean = true,
): Modifier {
    val handle = remember(fields, id) { FormFieldHandle(BringIntoViewRequester(), if (takesFocus) FocusRequester() else null) }
    DisposableEffect(fields, id, handle) {
        fields.register(id, handle)
        onDispose { fields.unregister(id, handle) }
    }
    return bringIntoViewRequester(handle.bringIntoView).let { tagged -> handle.focus?.let { tagged.focusRequester(it) } ?: tagged }
}
