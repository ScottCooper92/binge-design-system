# binge-design-system

The shared design system for [Binge](https://github.com/ScottCooper92) and the companion apps that
talk to it through [binge-companions](https://github.com/ScottCooper92/binge-companions).

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

The television layer is a module of its own, `designsystem-tv`, because Material 3 and tv-material
must not be mixed: they ship separate `MaterialTheme` trees, and the wrong import compiles cleanly
and renders subtly wrong. It carries the TV foundation that names none of Binge's types — the theme,
the focus units, the rail shell, the button, the card row and the message plate — and
`checkTvMaterialSeparation` holds the line. Binge's TV media card, media row and immersive backdrop
take its media-item model and stay there, as its error plate does.

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
├── DisplayFormatters.kt   formatRating, formatVoteCount, toInitials, badgeCountLabel,
│                          formatRuntime, the relative-or-absolute date formatter and month
│                          names
├── (root files) adaptive layout and fold posture, list-detail pane, nav overlay, pane insets,
│                icons, brushes, collapsing-title state and the shared aspect ratios
├── catalog/     one public …Sample() per component, the fixture every screenshot frame renders,
│                and the …Demo()s the catalog app runs live
├── modifier/    skeleton shimmer, selection lift
├── layout/      layout anchors
├── template/    the whole-screen frames: the scaffold, the message screen, the filtered list, the
│                hero detail page, the form, the step flow and the paged phase
└── preview/     @ComponentPreviews and the other device matrices, ScreenshotTheme

designsystem-tv/src/main/kotlin/com/binge/designsystem/tv/
├── theme/       the tv-material theme, projected from the same tokens
├── focus/       the focus groups, the indicator, arrival and scroll units
├── nav/         the navigation rail shell
├── component/   the buttons, the card row, the section title, the message plate, the initials
│                avatar, the QR code, the selected tick, the row emphasis and the vertical divider
├── layout/      layout anchors, so a skeleton can promise the geometry its content fills
├── template/    the whole-screen frames: the page hosting, the board, the two-pane page, the step
│                flow, the message page, the detail page and the immersive hub and grid
├── Dimens.kt    the non-dp constants the TV components share, such as the nav rail's collapsed row cap
├── catalog/     the TV samples
└── preview/     @TvPreviews and the TV screenshot theme

catalog-app/        the debug-only catalog app, with a phone activity and a TV one
catalog-registry/   the generator that lists the catalog's samples and demos for that app

docs/
└── tv-foundation.md   why focus is a parameter, and the accent model the TV components share
```

In each module every dp a component reads lives in `src/main/res/values/dimens.xml` (with the width and
orientation qualifiers next to it), and the screenshot baselines are under
`src/screenshotTestDebug/reference/`. Two things are exempt from the dimens rule: a literal in a private
`@Preview`, and the corner radii in `BingeShapes`, which are theme tokens. In `designsystem`, every
user-visible string is in `values/strings.xml` with its Spanish translation alongside; `designsystem-tv`
has no strings, because its components take their copy as parameters. The baselines are the ones Binge
recorded; each slice validated byte-identical against them before it landed.

## The catalog app

`catalog-app` is a debug-only Android app that lists every sample and demo in the catalog and shows
each one full screen on a device. It is for **behaviour a screenshot cannot show**: a real modal
window, a bar that changes as a list scrolls, a button going busy, D-pad focus on TV, a locale.
Appearance stays with the screenshot suite and Binge's screenshot gallery.

Run it from a checkout:

```sh
./gradlew :catalog-app:installDebug
```

It installs beside Binge and binge-seerr, with two launcher entries: a phone one, and a leanback one
for Android TV.

**An entry appears with nothing to register.** The app is generated from the sources:

- A public, no-parameter `@Composable fun …Sample()` in `designsystem/…/catalog/` or
  `designsystem-tv/…/tv/catalog/` is listed as a sample.
- A public, no-parameter `@Composable fun …Demo()` in `designsystem/…/catalog/` is listed as a demo.
  It runs the real component with real state. Demos are never screenshot fixtures.
- The first sentence of its KDoc is its description, and the app's search reads it.
- A public `…Sample` or `…Demo` that takes parameters, or is not composable, fails the build with its
  file and line. A private or internal one is ignored.

**One card per component.** Samples and demos are variants of a component, and the grid shows the
component once. A component is its samples file, without the `Samples` suffix. A demo joins the
component its function name starts with. A component's card shows a live, scaled-down render of its
first sample, or an icon when it has only demos. Components with a demo come first, under Demos.

**A component's page.** Tapping a card opens its page. Most components list every variant, one under
the next, so they compare at a glance. A component a screen holds one of, such as a top bar, shows one
variant at a time, and its variant is chosen in the tweaks sheet. A sample's controls work: a sample
keeps its own state, seeded with the values its screenshot shows.

**File annotations.** Four annotations in `catalog/` shape the page. They are source-only, and the
generator reads them as lines:

- `@file:OnePerScreen` shows the component one variant at a time. With `fullScreen = true` the
  variant replaces the app's own chrome. A top bar is only judged at the real top of the window, and
  its back button leaves through `LocalDemoBack`.
- `@file:CatalogGroup("Top app bars")` lists every entry in the file under that name. Components a
  screen chooses between share a page this way.
- `@file:ScreenshotOnly` keeps the file's samples out of the app. They stay screenshot fixtures, and
  a live demo stands in for them.
- `@file:SelfDescribing` says the samples carry their own description, such as a button labelled with
  its state. The app shows no description card over them.

A variant that scrolls vertically cannot sit in a list. A test composes every listed variant in a
lazy list, so it fails the build until that component is marked `OnePerScreen`.

**Layout.** The phone catalog is a two-column grid with a search field. From 840dp wide, on a tablet or
an unfolded foldable, the grid and the open component sit side by side, each in a `PaneContent`. The
app runs edge to edge, and its chrome is built from the design system's own components.

**Controls.** A floating button opens the tweaks: dark mode, font scale (1.0, 1.3, 2.0), RTL, and
language (system, English, Spanish, and the `en-XA` and `ar-XB` pseudolocales). They stay set as you
move between variants. The TV detail view has font scale and RTL: the TV theme is dark-only, so it has
no dark toggle. The first three work by overriding composition locals around the sample, so no sample
is edited. Resource qualifiers such as screen width are not reachable that way, which is why the app
has no width presets. Language works differently, by handing the sample a context recreated for the
locale.

**Never published.** There is no release variant, no signing configuration and no APK artifact.
`./gradlew build` compiles, lints and tests it with everything else, and nothing in Binge or
binge-seerr substitutes it, so a consumer never configures it. `checkTvMaterialSeparation` also scans
its `tv/` package.

## Status

**Pre-alpha, and consumed.** Binge and binge-seerr both include this build; Binge's own
`core/designsystem` keeps only the components that still name one of its types.

Four slices have landed: the theme, the preview scaffolding and the components that named nothing
of Binge's; then the display formatters and the components they unlocked; then the settings group
with the relative-date formatter; and the `designsystem-tv` module, the TV foundation. Each arrived
with its tests and baselines, and each is followed by a Binge PR that deletes its copies and
re-points imports; between the two, Binge builds from its own copies of whatever the latest slice
moved.

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
        substitute(module("com.binge:designsystem-tv")).using(project(":designsystem-tv"))
    }
}
```

Then depend on it as `implementation("com.binge:designsystem")`. The substitution is what lets the
consumer name a coordinate rather than a path, so moving to a published artifact later is a change
to `settings.gradle.kts` and nothing else.

A submodule pins a commit, so a consumer updates deliberately — `git submodule update --remote` —
rather than being moved by whatever landed here today. That is the property that makes source-level
sharing survivable across three repositories.

## Test fixtures

Both modules ship `testFixtures` for a consumer's Robolectric tests. Depend on them with
`testImplementation(testFixtures("com.binge:designsystem"))` and the same for `designsystem-tv`. They are the one copy:
a consumer keeps no wrappers of its own.

- `designsystem`: `createKeyboardComposeRule()`, for a test that drives focus with keys; the take-down rules
  (`createTakeDownComposeRule()` and its keyboard and activity forms), for a suite that collects `LazyPagingItems`;
  `ShadowMeshSpecification`; and the skeleton-geometry helpers.
- `designsystem-tv`: `LeanbackRule`, which makes the device a TV so the pivot scroll applies; `settle()`, with
  `SETTLED_FOCUS_WAIT_MILLIS` and `ANCHOR_COALESCE_WAIT_MILLIS` for the immersive pages' debounce and coalesce
  windows; and `TvLateTarget`.

## Licence

See [LICENSE](LICENSE).
