# binge-design-system

The shared design system for [Binge](https://github.com/ScottCooper92) and the companion apps that
talk to it through [binge-integrations](https://github.com/ScottCooper92/binge-integrations).

## Why this exists

A companion app is a separate APK. It has no dependency on Binge and never will — that separation is
the whole architecture. But a companion's screens sit inside the same experience, and a user moving
between them should not feel the seam.

So the components are shared and the product code is not.

## What belongs here

Components with **no coupling to Binge's own domain**. Measured strictly — a file counts only if
nothing it references, directly or through another design-system file, names a Binge type. The
first slice moved the 111 files that passed that test as they stood; the second moved the display
formatting the components render — a score, a vote count, a name's initials, a badge count — in
here, which is where it belongs, and the 40 components that only ever named Binge through those.

What stays in Binge is everything that names a TMDB type, an account, a navigation route, or
Binge's error model, and everything that renders one of those. The line now runs through the
media-item model (the hub and poster carousels take Binge's `MediaItemUi`, which carries a TMDB
media type), the error screens (typed against Binge's `UiError`), and the request-state UI, which
is typed against the integration contract rather than against a design system and belongs with the
SDK a companion already depends on.

## What is here

```
designsystem/src/main/kotlin/com/binge/designsystem/
├── theme/       BingeColors, BingeShapes, the expressive theme, typography, contrast
├── component/   the shared M3 components: the nav shell, buttons, chips, top bars, sheets, cards,
│                rows, tiles, the hero carousel, the skeletons
├── DisplayFormatters.kt   formatRating, formatVoteCount, toInitials, badgeCountLabel
├── catalog/     one public …Sample() per component, the fixture every screenshot frame renders
├── modifier/    skeleton shimmer, selection lift
├── layout/      layout anchors
└── preview/     @ComponentPreviews and the other device matrices, ScreenshotTheme
```

Every dp lives in `src/main/res/values/dimens.xml` (with the width and orientation qualifiers next to
it), every user-visible string in `values/strings.xml` with its Spanish translation alongside, and
the screenshot baselines under `src/screenshotTestDebug/reference/`. The baselines are the ones Binge
recorded; each slice validated byte-identical against them before it landed.

## Status

**Pre-alpha, and consumed.** Binge and binge-seerr both include this build; Binge's own
`core/designsystem` keeps only the components that still name one of its types.

Two slices have landed: the theme, the preview scaffolding and the components that named nothing
of Binge's, then the display formatters and the components they unlocked. Each arrived with its
tests and baselines, and each is followed by a Binge PR that deletes its copies and re-points
imports; between the two, Binge builds from its own copies of whatever the latest slice moved.

## How it is consumed

Source-level, via a git submodule plus `includeBuild` — not a published artifact.

That is deliberate. Publishing to Maven Central commits to a stable public API, and these components
are still moving. A submodule gives both consumers the same source with no version skew and no API
promise to keep. Maven Central stays available later, when the surface has settled.

The catalog at the root is load-bearing for the same reason: a consumer includes this build, so a
Compose or AGP disagreement here is a disagreement in *their* build.

In the consumer, once as a submodule:

```bash
git submodule add https://github.com/ScottCooper92/binge-design-system.git design-system
```

and in its `settings.gradle.kts`:

```kotlin
includeBuild("design-system") {
    dependencySubstitution {
        substitute(module("com.binge:designsystem")).using(project(":designsystem"))
    }
}
```

Then depend on it as `implementation("com.binge:designsystem")`. The substitution is what lets the
consumer name a coordinate rather than a path, so moving to a published artifact later is a change
to `settings.gradle.kts` and nothing else.

A submodule pins a commit, so a consumer updates deliberately — `git submodule update --remote` —
rather than being moved by whatever landed here today. That is the property that makes source-level
sharing survivable across three repositories.

## Licence

See [LICENSE](LICENSE).
