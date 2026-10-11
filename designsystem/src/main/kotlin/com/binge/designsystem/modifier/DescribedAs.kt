package com.binge.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Names a loading placeholder for a screen reader when [description] is given; otherwise leaves it silent.
 * Semantics only, so it renders nothing and works under either theme tree, phone or TV.
 */
fun Modifier.describedAs(description: String?): Modifier = if (description == null) this else semantics { contentDescription = description }
