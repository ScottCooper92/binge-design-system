package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeConfirmDialogContent
import com.binge.designsystem.component.ExpandableOverview
import com.binge.designsystem.component.RatingCard
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Components whose copy comes from the design system's own string resources, stacked so a locale
 * change in the catalog app is visible at a glance: the rating prompt, the overview's more/less
 * toggle and the dialog's default Cancel. Switch the locale in the detail view to see them re-render.
 */
@Composable
fun LocalisedStringsDemo() {
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
        ) {
            RatingCard(
                userRating = null,
                isSignedIn = true,
                reviewCount = 7,
                averageReviewRating = 7.5f,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
            ExpandableOverview(text = LOCALE_DEMO_OVERVIEW)
            BingeConfirmDialogContent(
                title = "Delete list?",
                message = "This removes the list and everything in it.",
                confirmLabel = "Delete",
                onConfirm = {},
                onDismissRequest = {},
            )
        }
    }
}

private const val LOCALE_DEMO_OVERVIEW =
    "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept " +
        "one of the greatest psychological and physical tests of his ability to fight injustice. Gotham's new " +
        "district attorney Harvey Dent joins the cause, but the trio soon find themselves prey to a reign of terror."
