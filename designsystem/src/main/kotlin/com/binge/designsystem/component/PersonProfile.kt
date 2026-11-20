package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.template.readingMargin
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.toInitials

/** One line of a person's profile: a [value] such as a birthday or a place, under its [label] when it has one. The caller owns both words. */
@Immutable
class PersonFact(
    val value: String,
    val label: String? = null,
)

/**
 * The top of a person's page: a portrait, an optional [eyebrow] over the [name], and the [facts] under it, a label over each value, or a plain line
 * where a fact has no label. The portrait is two thirds as wide as it is tall; with no [photoUrl], or while it loads or if it fails, the person's
 * initials stand in. The eyebrow is set in capitals in the user's language. The name is a heading and wraps rather than ellipsizing.
 *
 * It starts where the page's reading column does and takes the screen's content inset, so it lines up with the sections under it. It carries the
 * [LayoutAnchors.Detail.PROFILE] anchor, which [PersonProfileSkeleton] shares.
 *
 * @need show the top of a person's page with a portrait, a name and a few facts
 * @group detail
 * @replaces a hand-built Row of a portrait and a column of text
 */
@Composable
fun PersonProfileHeader(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    facts: List<PersonFact> = emptyList(),
) {
    ProfileRow(modifier) {
        PersonPortrait(name = name, photoUrl = photoUrl)
        Column(Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(LocalConfiguration.current.locales[0]),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            facts.forEach { fact ->
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
                FactLine(fact)
            }
        }
    }
}

/**
 * The loading stand-in for [PersonProfileHeader]: a portrait plate, a name plate and [facts] short lines, in the header's own room and under the
 * same anchor, so the page does not move when the person arrives.
 *
 * What a line of text takes depends on its words, which a loading state does not have, so each plate is one line unless the caller hints at its
 * text. [expectedEyebrow], [expectedName] and [expectedFacts] are strings the loaded text is expected to look like (the longest typical name, the
 * usual facts). The plate lays that string out invisibly in the real style and the column's own width, so it takes what the text takes at every font
 * size, wrapping included, and a page whose text is that long does not move. A hint sets the count of facts in place of [facts]. With no hint the
 * plates are as they were.
 *
 * @need show a loading placeholder for the top of a person's page
 * @group feedback
 */
@Composable
fun PersonProfileSkeleton(
    modifier: Modifier = Modifier,
    facts: Int = 3,
    expectedEyebrow: String? = null,
    expectedName: String? = null,
    expectedFacts: List<PersonFact> = emptyList(),
) {
    ProfileRow(modifier) {
        Box(
            Modifier
                .width(dimensionResource(R.dimen.person_profile_photo_width))
                .aspectRatio(PORTRAIT_ASPECT)
                .skeleton(visible = true, shape = BingeShapes.ProfilePoster),
        )
        Column(Modifier.weight(1f)) {
            val plateWidth = Modifier.width(dimensionResource(R.dimen.skeleton_header_width))
            expectedEyebrow?.let {
                TextPlate(
                    MaterialTheme.typography.labelSmall,
                    Modifier.fillMaxWidth(),
                    text = it.uppercase(LocalConfiguration.current.locales[0]),
                )
            }
            if (expectedName != null) {
                TextPlate(MaterialTheme.typography.headlineMedium, Modifier.fillMaxWidth(), text = expectedName)
            } else {
                TextPlate(MaterialTheme.typography.headlineMedium, plateWidth)
            }
            if (expectedFacts.isNotEmpty()) {
                expectedFacts.forEach { fact ->
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
                    fact.label?.let {
                        TextPlate(
                            MaterialTheme.typography.labelSmall,
                            Modifier.fillMaxWidth(),
                            text = it.uppercase(LocalConfiguration.current.locales[0]),
                        )
                    }
                    TextPlate(MaterialTheme.typography.bodyLarge, Modifier.fillMaxWidth(), text = fact.value)
                }
            } else {
                repeat(facts) {
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
                    TextPlate(MaterialTheme.typography.labelSmall, plateWidth)
                    TextPlate(MaterialTheme.typography.bodyLarge, plateWidth)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier =
            modifier
                .layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE))
                .readingMargin()
                .padding(horizontal = resolvedContentInset(), vertical = dimensionResource(R.dimen.padding_s)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
        verticalAlignment = Alignment.Top,
        content = content,
    )
}

@Composable
private fun FactLine(fact: PersonFact) {
    Column {
        if (fact.label != null) {
            Text(
                text = fact.label.uppercase(LocalConfiguration.current.locales[0]),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = fact.value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PersonPortrait(name: String, photoUrl: String?) {
    Box(
        Modifier
            .width(dimensionResource(R.dimen.person_profile_photo_width))
            .aspectRatio(PORTRAIT_ASPECT)
            .clip(BingeShapes.ProfilePoster),
    ) {
        if (photoUrl.isNullOrBlank()) {
            PortraitInitials(name)
        } else {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { PortraitInitials(name) },
                error = { PortraitInitials(name) },
            )
        }
    }
}

@Composable
private fun PortraitInitials(name: String) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        Text(
            text = name.toInitials(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

private const val PORTRAIT_ASPECT = 2f / 3f
