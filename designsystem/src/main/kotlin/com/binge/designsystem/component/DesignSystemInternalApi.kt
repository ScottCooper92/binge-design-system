package com.binge.designsystem.component

/**
 * Marks what the design system's own modules share and an app must not build on: it can change with no notice to the four builds that include
 * this one. The design system's modules opt in to it; nothing else should.
 */
@RequiresOptIn(
    message = "Shared between the design system's own modules; an app must not build on it.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
annotation class DesignSystemInternalApi
