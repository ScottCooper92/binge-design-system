package com.binge.designsystem.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.bottomBarInsets
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeShapes

/**
 * The pinned bar under a screen: a raised surface with the top corners rounded, that clears the navigation bar
 * and a side cutout and keeps the content's side inset. Pass it as `BingeScreenScaffold`'s `bottomBar`; [content]
 * is whatever the bar holds, a summary line or a row of buttons. [FormScreen]'s footer is built on it, so every
 * pinned bar sits the same over what scrolls under it.
 *
 * It does not lift itself above the keyboard; a caller whose bar holds a field adds `imePadding` around it.
 */
@Composable
fun ScreenFooter(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = BingeShapes.HeroTop,
        shadowElevation = dimensionResource(R.dimen.snackbar_elevation),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(bottomBarInsets())
                    .padding(horizontal = resolvedContentInset())
                    .padding(top = dimensionResource(R.dimen.padding_sm), bottom = dimensionResource(R.dimen.padding_m)),
            content = content,
        )
    }
}
