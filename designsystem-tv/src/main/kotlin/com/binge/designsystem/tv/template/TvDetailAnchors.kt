package com.binge.designsystem.tv.template

import com.binge.designsystem.tv.layout.TvLayoutAnchors

/**
 * The layout anchors on the detail page, which its loading state reserves. The loading page and the loaded one tag
 * the same points, so a test measures that resolving moves none of them.
 */
object TvDetailAnchors {
    /** The action row's primary button. */
    val PRIMARY_ACTION: String = TvLayoutAnchors.entry("detail-v2-primary-action")

    /** The meta line under the logo. */
    val META: String = TvLayoutAnchors.entry("detail-v2-meta")

    /** The action row's icon button at [index], among the icon buttons: each is tagged, so a count that drifts fails too. */
    fun iconAction(index: Int): String = TvLayoutAnchors.entry("detail-v2-icon-action-$index")

    /** The tab strip's tab at [index], each tagged for the same reason. */
    fun tab(index: Int): String = TvLayoutAnchors.entry("detail-v2-tab-$index")
}
