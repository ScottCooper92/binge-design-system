@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.BingeActionFooter
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.ScreenFooter

/**
 * Public catalog samples for the containers / chrome group — sheets, dialogs, top bars, scaffolding.
 * Follows the convention on [MediaCardRatedSample]: a no-arg `@Composable` wrapped in [ScreenshotTheme]
 * that both the catalog `@Preview` and the matching screenshot test render. Modal windows don't
 * capture in screenshots, so samples target the stateless `*SheetContent`/`*DialogContent`, not the
 * modal wrapper.
 */
@Composable
fun BingeActionFooterSample() {
    ScreenshotTheme {
        BingeActionFooter(label = "Show results", onClick = {})
    }
}

/**
 * The same footer on a page rather than a sheet: no band of its own, and no bottom spacing where the
 * host's scaffold has already inset. Paired with [BingeActionFooterSample] because the two together
 * are what make the container parameter's job legible — one shows the band, the other its absence.
 */
@Composable
fun BingeActionFooterOnPageSample() {
    ScreenshotTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            BingeActionFooter(
                label = "Save",
                onClick = {},
                containerColor = Color.Transparent,
                bottomPadding = 0.dp,
            )
        }
    }
}

/**
 * The footer's action carrying a [BingeActionFooter.leadingIcon] — the same action shown elsewhere as
 * a tiered [com.binge.designsystem.component.BingeOutlinedButton]/[com.binge.designsystem.component.BingeTextButton]
 * tile, promoted here to the sheet's sole primary CTA. Paired with [BingeActionFooterSample] to make
 * the icon's presence legible as a variant of the same container.
 */
@Composable
fun BingeActionFooterWithIconSample() {
    ScreenshotTheme {
        BingeActionFooter(
            label = "Open on your server",
            onClick = {},
            leadingIcon = Icons.AutoMirrored.Filled.OpenInNew,
        )
    }
}

/**
 * The page form: a page whose primary action anchors in a `bottomBar` slot outside the scaffold's own body is a
 * [ScreenFooter] holding a filled button, not a [BingeActionFooter]. Rounded and raised so it reads as its own surface.
 */
@Composable
fun BingeActionFooterElevatedSample() {
    ScreenshotTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ScreenFooter { BingeFilledButton(label = "Add a slider", onClick = {}, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

/**
 * The footer while its action runs: the label gives way to a spinner and the tap is ignored, so a
 * second tap cannot start the same call twice.
 */
@Composable
fun BingeActionFooterLoadingSample() {
    ScreenshotTheme {
        BingeActionFooter(label = "Show results", onClick = {}, loading = true)
    }
}

/**
 * The footer for a sheet whose one action removes or declines something: the same band and button in
 * the error tone, as on a request's decline-with-reason sheet.
 */
@Composable
fun BingeActionFooterDestructiveSample() {
    ScreenshotTheme {
        BingeActionFooter(label = "Decline request", onClick = {}, destructive = true)
    }
}

/**
 * A failed send from a sheet's pinned primary: the button in the error tone with the short reason as its label, which the caller shows
 * for a few seconds. A tap is the caller's, usually opening a dialog with the full cause and Retry. Not [BingeActionFooterDestructiveSample]:
 * that is a sheet whose action removes or declines something, and the failure is a send that did not go through.
 */
@Composable
fun BingeActionFooterFailedSample() {
    ScreenshotTheme {
        BingeActionFooter(label = "Couldn't reach your server", onClick = {}, failed = true)
    }
}

/** The same failure in Spanish, whose reason runs longer: at a large font it wraps and the button grows rather than clip it. */
@Composable
fun BingeActionFooterFailedSpanishSample() {
    ScreenshotTheme {
        BingeActionFooter(label = "No se pudo conectar con tu servidor", onClick = {}, failed = true)
    }
}
