package com.binge.designsystem

/**
 * Placeholder for the design system this repository will hold.
 *
 * The carve (#2427 in Binge) moves the components that have no coupling to Binge's own domain —
 * 173 of its 218 design-system files as measured on 2026-09-11. Until then this exists so the build
 * is real and CI has something to compile and test, because every agent workflow here hangs off a
 * successful CI run and a repository with no build gives them nothing to trigger on.
 */
object BingeDesignSystem {
    /**
     * The consumers this build is shared with by `includeBuild`.
     *
     * Named rather than counted: the whole point of carving is that a companion author gets the
     * same components Binge uses, so "who is this for" is the one fact worth stating before the
     * components arrive.
     */
    val CONSUMERS: List<String> = listOf("Binge", "binge-seerr")
}
