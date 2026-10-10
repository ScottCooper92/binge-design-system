package com.binge.designsystem.testing

import androidx.compose.runtime.Composable
import com.binge.designsystem.theme.BingeExpressiveTheme

/** The theme a Robolectric test hosts its content in: the expressive theme without dynamic colour, so it is the same on every machine. */
@Composable
internal fun TestTheme(content: @Composable () -> Unit) = BingeExpressiveTheme(dynamicColor = false, content = content)
