package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * The filled button, and the emphasis default for a screen's one primary action.
 *
 * [destructive] carries the same meaning here as on [BingeOutlinedButton]: the action commits
 * something irreversible. It chooses the [colors] default rather than being read separately, so an
 * explicit [colors] still wins and a caller that already passes one renders exactly as before.
 *
 * While [loading] the button shows a spinner and swallows taps, so an in-flight action cannot be re-triggered; set
 * [tapsWhileLoading] for a button whose tap is not that action, such as one that opens a sheet that shows the loading itself.
 *
 * [leading] is drawn before the icon and the label, for a mark that is not an icon, such as [BingeStatusButton]'s status dot.
 *
 * The fill is the fixed amber, the same in both themes. Light `primary` is a darker amber tuned for
 * text, and as a large fill it reads muddy.
 *
 * @need offer the primary action on a screen
 * @group inputs
 * @sample FilledButtonSample
 * @replaces a raw Button with colours and loading state set by hand
 */
@Composable
fun BingeFilledButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    tapsWhileLoading: Boolean = false,
    destructive: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryFixedDim,
        contentColor = if (destructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryFixed,
    ),
    leading: (@Composable () -> Unit)? = null,
) {
    Button(
        // Swallow taps while loading so the in-flight action can't be re-triggered,
        // without greying the button out (it stays filled with a spinner in place of the label).
        onClick = { if (!loading || tapsWhileLoading) onClick() },
        modifier = modifier.defaultMinSize(
            minWidth = ButtonDefaults.MinWidth,
            minHeight = dimensionResource(R.dimen.button_filled_height),
        ),
        shape = BingeShapes.Medium,
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.button_filled_padding_h),
            vertical = dimensionResource(R.dimen.zero),
        ),
        colors = colors,
    ) {
        ButtonContent(label = label, loading = loading, contentColor = colors.contentColor, leadingIcon = leadingIcon, leading = leading)
    }
}
