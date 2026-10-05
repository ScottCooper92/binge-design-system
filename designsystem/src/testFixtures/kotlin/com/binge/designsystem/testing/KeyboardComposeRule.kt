package com.binge.designsystem.testing

import androidx.compose.ui.input.InputMode
import androidx.compose.ui.test.ComposeUiTestConfig
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlin.time.Duration

/**
 * The Compose test rule for a test that drives focus with keys: a TV's D-pad, or Tab on a hardware
 * keyboard. It is `createComposeRule()` in keyboard input mode.
 *
 * From Compose UI test 1.13, `createComposeRule()` starts every test in touch mode. `clickable` refuses
 * focus in touch mode, so a plain rule fails every focus assertion on a clickable target. A TV has no
 * touchscreen and never enters touch mode, so this is the faithful fixture, not a workaround. A phone
 * test that taps keeps the plain rule.
 */
fun createKeyboardComposeRule(): ComposeContentTestRule = createComposeRule(KeyboardComposeUiTestConfig)

/**
 * [createKeyboardComposeRule]'s config. The timeout is infinite because the plain `createComposeRule()`
 * it stands in for enforces none.
 */
val KeyboardComposeUiTestConfig: ComposeUiTestConfig =
    ComposeUiTestConfig(inputMode = InputMode.Keyboard, testTimeout = Duration.INFINITE)
