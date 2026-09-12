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
nothing it references, directly or through another design-system file, names a Binge type — that is
111 of Binge's 218 design-system files, and they are what the first slice moved.

What stays in Binge is everything that names a TMDB type, an account, a navigation route, or one of
Binge's own formatters, and everything that renders one of those. The line runs through a few
components a companion would plausibly want — the poster card, the rating chip, the initials
avatar — because each takes a Binge formatter or a domain enum today. Each of those is a small
change on the Binge side (take the formatted value as a parameter), and it moves once that lands.
What is *also* not here, despite looking like it should be, is the request-state UI: those
components are typed against the integration contract rather than against a design system, so they
belong with the SDK a companion already depends on.

## What is here

```
designsystem/src/main/kotlin/com/binge/designsystem/
├── theme/       BingeColors, BingeShapes, the expressive theme, typography, contrast
├── component/   the shared M3 components: buttons, chips, top bars, sheets, rows, tiles, skeletons
├── catalog/     one public …Sample() per component, the fixture every screenshot frame renders
├── modifier/    skeleton shimmer, selection lift
├── layout/      layout anchors
└── preview/     @ComponentPreviews and the other device matrices, ScreenshotTheme
```

Every dp lives in `src/main/res/values/dimens.xml` (with the width and orientation qualifiers next to
it), every user-visible string in `values/strings.xml` with its Spanish translation alongside, and
the screenshot baselines under `src/screenshotTestDebug/reference/`. The baselines are the ones Binge
recorded; the first slice validated byte-identical against them before it landed.

## Status

**Pre-alpha. The first slice of the carve is here; Binge does not consume it yet.**

The theme, the preview scaffolding and the uncoupled components arrived in one move, with their
tests and baselines. Binge's wiring of this build — the submodule, the `includeBuild`, and
re-pointing its imports — is a separate PR on Binge's side, and until it lands Binge still builds
from its own copies. Nothing here is consumed by a companion yet either.

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
