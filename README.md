# binge-design-system

The shared design system for [Binge](https://github.com/ScottCooper92) and the companion apps that
talk to it through [binge-integrations](https://github.com/ScottCooper92/binge-integrations).

## Why this exists

A companion app is a separate APK. It has no dependency on Binge and never will — that separation is
the whole architecture. But a companion's screens sit inside the same experience, and a user moving
between them should not feel the seam.

So the components are shared and the product code is not.

## What belongs here

Components with **no coupling to Binge's own domain**. As measured on 2026-09-11, that was 173 of
Binge's 218 design-system files — 79%.

What stays in Binge is everything that names a TMDB type, an account, or a navigation route. What is
*also* not here, despite looking like it should be, is the request-state UI: those components are
typed against the integration contract rather than against a design system, so they belong with the
SDK a companion already depends on.

## Status

**Pre-alpha, and the carve has not happened yet.**

What is here is the build, the agent workflows and a placeholder. The components arrive from Binge
in a single move; this exists first so the move lands somewhere that already builds and reviews.

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
