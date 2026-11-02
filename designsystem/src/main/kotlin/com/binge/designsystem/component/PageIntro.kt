package com.binge.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R

/**
 * The sentence under a page's title that says what the page is for. One size and one inset in every app: `bodyLarge`, inset
 * horizontally by the section headings' own inset so the intro, the headings and the card edge below share a left edge, and
 * followed by a fixed gap.
 *
 * It is page context and not a heading, so it carries no heading semantics: a screen reader reads it where it sits, straight
 * after the title, and heading navigation skips it. Keep it to a sentence; a line that wraps to one orphan word wants shorter
 * copy, not smaller type.
 */
@Composable
fun PageIntro(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            modifier
                .padding(horizontal = dimensionResource(R.dimen.padding_s))
                .padding(bottom = dimensionResource(R.dimen.page_intro_spacing_bottom)),
    )
}
