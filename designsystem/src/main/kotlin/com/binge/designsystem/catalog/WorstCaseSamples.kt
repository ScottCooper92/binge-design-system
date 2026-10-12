@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeChoice
import com.binge.designsystem.component.BingeChoiceList
import com.binge.designsystem.component.BingeChoiceSheetContent
import com.binge.designsystem.component.BingeInitialsAvatar
import com.binge.designsystem.component.BingeTag
import com.binge.designsystem.component.CheckboxRow
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.ListItemDestination
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.ListRowHeader
import com.binge.designsystem.component.ListRowPoster
import com.binge.designsystem.component.MediaCard
import com.binge.designsystem.component.MediaCardAction
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.component.MediaTypeTag
import com.binge.designsystem.component.MediaTypeTagType
import com.binge.designsystem.component.WrappingRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

/*
 * The shared rows at their worst: every optional line filled, long text, nothing short. They are the fixtures for the
 * no-clipping test, which renders each at font scales 1.3 and 2.0 and fails if any text is cut off by its parent, and
 * for the frames that show the same at a glance. A row that is fine on a short title and a small font is the usual
 * way a clip ships, so these are the rows a new component is measured against.
 */

private const val LONG_TITLE = "The Lord of the Rings: The Rings of Power, Extended Edition with Every Commentary Track"
private const val LONG_LINE =
    "Needs the application URL and email notifications set up on the server, or the new user cannot sign in with the " +
        "password that is sent. Set both under General and Notifications first."

/** A [ListRow] with a poster, a title that wraps, a header badge, a status line that wraps, a season line, a requester, a trailing action and a footer. */
@Composable
fun ListRowWorstCaseSample() {
    ScreenshotTheme {
        ListRow(
            onClick = {},
            leading = { ListRowPoster(imageUrl = null, contentDescription = null) },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            footer = { LinearProgressIndicator(progress = { 0.6f }, modifier = Modifier.fillMaxWidth()) },
        ) { contentModifier ->
            Column(modifier = contentModifier.padding(top = dimensionResource(R.dimen.detail_meta_spacing))) {
                ListRowHeader(title = LONG_TITLE) { BingeTag(label = "New") }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
                WrappingRow {
                    BingeTag(label = "Partly available")
                    MediaTypeTag(type = MediaTypeTagType.Tv)
                    Text("4K", style = MaterialTheme.typography.labelMedium)
                    Text("2022", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
                Text(
                    text = "Seasons 1, 2 and 4–6, and every special",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f).heightIn(min = dimensionResource(R.dimen.padding_xs)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BingeInitialsAvatar(name = "Alexandria Fitzgerald-Montgomery")
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
                    Text(
                        text = "Alexandria Fitzgerald-Montgomery · September 27, 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** An [ItemGroup] with a long title and rows that fill the label, the detail, a count, an alert, an external destination and a selected state. */
@Composable
fun ItemGroupWorstCaseSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Notifications and everything that decides who is told, and when",
            rows =
                listOf(
                    ListItem(
                        icon = Icons.Filled.Notifications,
                        label = "Email notifications for every request, issue and comment",
                        detail = LONG_LINE,
                        badgeCount = 128,
                        selected = true,
                        supportingMaxLines = 3,
                    ),
                    ListItem(
                        icon = Icons.Filled.Policy,
                        label = "Privacy policy and the terms of the hosted service",
                        detail = "Opens in your browser",
                        badgeAlert = true,
                        destination = ListItemDestination.External,
                    ),
                ),
        )
    }
}

/** A [CheckboxRow] whose label and supporting line run long, with an icon and a trailing status. */
@Composable
fun CheckboxRowWorstCaseSample() {
    ScreenshotTheme {
        CheckboxRow(
            label = "Season 12: the one with the longest title in the whole list of seasons",
            supportingText = LONG_LINE,
            supportingMaxLines = 3,
            checked = true,
            onCheckedChange = {},
            icon = Icons.Filled.Tv,
            trailingContent = {
                Text(
                    "Available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}

/** A choice sheet whose choices carry a long label, three lines of support, a mark, an avatar and a trailing count. */
@Composable
fun BingeChoiceWorstCaseSample() {
    ScreenshotTheme {
        BingeChoiceSheetContent(
            title = "Choose where the new request is sent",
            subtitle = "Every server this account can reach, with the quality profile each one uses",
            choices =
                BingeChoiceList.Ready(
                    listOf(
                        BingeChoice(
                            value = "a",
                            label = "Living room 4K server with the very longest name anyone has given a server",
                            supportingText = LONG_LINE,
                            supportingMaxLines = 3,
                            mark = "4K",
                            trailingText = "1,284",
                        ),
                        BingeChoice(
                            value = "b",
                            label = "Alexandria Fitzgerald-Montgomery",
                            supportingText = "alexandria.fitzgerald-montgomery@example.com",
                            avatarName = "Alexandria Fitzgerald-Montgomery",
                            trailingText = "12",
                        ),
                    ),
                ),
            selected = "a",
            onSelect = {},
        )
    }
}

/** A [MediaCard] with a long title, a rating, a type badge, a long status, a top-end action and the viewer's own rating. */
@Composable
fun MediaCardWorstCaseSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = LONG_TITLE,
            rating = 7.9f,
            userRating = 9f,
            onClick = {},
            typeBadge = MediaTypeTagType.Tv,
            topEndAction = MediaCardAction(icon = Icons.Filled.Close, contentDescription = "Remove", onClick = {}),
            status = MediaCardStatus(label = "Partly available", sentiment = BingeSentiment.Positive),
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}
