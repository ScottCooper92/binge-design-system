package com.binge.designsystem.component

/**
 * Which page this skeleton is standing in for.
 *
 * The four detail screens share one placeholder, but they do not share one shape, and a skeleton drawn for
 * the wrong one is worse than none: it reserves space the arriving page does not want, so resolving is a
 * relayout (#1598). The shape is one enum rather than the television's two orthogonal parameters
 * (`TvDetailSkeletonHero` × `TvDetailSkeletonPeek`) because on the phone the differences are not
 * orthogonal — the person page differs in its header, in having no stat row, and in what follows, all at
 * once — so a parameter matrix would spell out combinations no screen has.
 *
 * Each shape names the sections it promises, which is what the geometry check measures against the resolved
 * page ([LayoutAnchors]).
 */
enum class DetailSkeletonShape {
    /** Movie and show detail: hero → stats → action row → overview → cast rail. */
    Media,

    /**
     * Episode detail, signed in: the same hero and stats, but a rating card where the media pages put their
     * action row — the episode page has no play/request controls to stand in for.
     */
    Episode,

    /**
     * Episode detail, signed out: hero → stats → overview, with nothing between. TMDB requires a session to
     * rate, so `EpisodeRatingCard` renders nothing at all for a signed-out user — not even its own leading
     * spacer — and [Episode]'s plate would promise a card that page never fills (#1991). Which of the two the
     * screen picks is a question about the *viewer*, not the episode, so it is answered from session state
     * rather than from the content being loaded.
     */
    EpisodeSignedOut,

    /**
     * Person detail: a bounded profile block where the media pages put a full-bleed hero, no stat row, and
     * the biography directly beneath. Nothing below that is promised, because the gallery strip and the
     * known-for rail are both conditional on data — reserving a rail the page may not have is the same
     * defect in the other direction.
     */
    Person,
}
