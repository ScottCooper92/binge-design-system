# CLAUDE.md

Conventions for this repository. The agent workflows in `.github/workflows/` read this file from
`main` and treat it as the source of truth; so should you.

## What this repository is

The design system shared by Binge and the companion apps. Read `README.md` for why it exists and
what belongs in it.

Status is pre-alpha, and consumed by both Binge and binge-seerr. Three slices of the carve from
Binge are here: the theme, the preview and screenshot scaffolding and every component that named
none of Binge's types; the display formatters (`DisplayFormatters.kt`) and the components they
unlocked — the nav shell, the poster card and carousel, the hero, the chips, the skeletons; and the
settings group with the relative-date formatter, for the companion's own settings screens. Binge
still holds the components that name its media-item model, its error model or the integration
contract.

## The one rule everything else serves

**A component here may not know what Binge's data looks like.**

No TMDB types, no account model, no navigation routes, no provider names. A component takes what it
renders as parameters and gives back callbacks. The display formatting a component applies to a
plain value — a score to one decimal, a count to `1.5k`, a name to its initials — lives here too, in
`DisplayFormatters.kt`, so that every app rendering the component formats it the same way. If it needs to name one of Binge's concepts to do
its job, it is not a shared component and it stays in Binge.

The test is not "does it compile here" — almost anything can be made to. It is whether a companion
author with no knowledge of Binge could use it and have it mean something.

## Consumers, and what that costs

Two builds include this one with `includeBuild`: Binge, and binge-seerr. There is no published
artifact and no version negotiation, which buys a lot and costs one specific thing:

**A green build here does not mean a green build there.** This repository cannot compile its
consumers. A changed public signature is a change in two other codebases that will not notice until
their next sync, so a PR that changes one says so in its body.

Corollary: the root `libs.versions.toml` is not a local decision. Compose and AGP versions have to
agree with the consumers'.

## Compose conventions

- Every `Dp` comes from a dimension resource, never an inline `.dp` in a composable.
- Reach for a shape token rather than re-deriving a `RoundedCornerShape`.
- User-visible text is a string resource in this module. A component that takes its copy as a
  parameter is usually the better shape here, because the consumer owns the words.
- A component takes a `Modifier` parameter, defaulted, and applies it to its outermost node.
- Previews are `@Preview`-annotated and render without a network or an injected dependency.
- Every component has a `…Sample()` in `catalog/` and a `@PreviewTest` frame that renders it, and a
  new visual variant gets its frame in the same PR. The catalog sample is the one public fixture for
  a component, so the screenshot test renders the sample rather than hand-rolling the same state.
- A `@Preview(name = …)` token is short, lowercase and space-free: it is baked into the baseline's
  filename, and a long one breaks out of Windows' path limit under a worktree.

## Gates

CI runs `./gradlew build validateDebugScreenshotTest`. `build` is the Kotlin compile, the unit
tests, `ktlintCheck` and Android lint — the ktlint plugin and AGP both wire themselves into `check`,
and `build` depends on it. The screenshot task is named on top because the screenshot plugin does
not hook `check`.

**The screenshot suite is the gate that matters most here.** A component's visible contract is how
it renders, and `validateDebugScreenshotTest` compares every `@PreviewTest` frame against the PNG
committed under `src/screenshotTestDebug/reference/`. When a change is meant to alter a frame,
re-record with `./gradlew updateDebugScreenshotTest`, look at the regenerated PNGs, and commit them
in the same commit as the code. Committing that output is the only way to accept a change; there is
no override flag.

There is no coverage floor and no custom convention task in this repository, so do not look for one
and do not report a finding as though one had caught it.

**Never silence a gate instead of fixing it.** No ktlint baseline, no `ktlint-disable`, no
`lint-baseline.xml`, no `abortOnError = false`. A baseline here is a finding suppressed in two
codebases that never agreed to it.

## Follow-ups

Anything worth doing that does not belong in the diff in front of you becomes a GitHub issue, not a
TODO comment and not a line in a PR description.

The test is **scope, not severity**. A low-severity problem that reaches beyond the current diff is
still an issue; a serious problem inside the diff is a change to make now.

## Commits and pull requests

- Conventional-commit subjects (`feat:`, `fix:`, `docs:`, `chore:`), imperative mood.
- One reviewable idea per PR.
- A PR that changes a public signature says what it means for the consumers.
- Documentation here is plain, direct English — short sentences, one idea each.

## Agent workflows

`.github/workflows/` holds a review bot and three author bots, called from
[binge-ci](https://github.com/ScottCooper92/binge-ci). They act only on PRs carrying the `agent`
label.

**That label is maintainers-only.** Applying it grants an agent code execution with this
repository's secrets in scope. Do not apply it to a PR you have not read, and never to one from a
fork — the workflows already refuse fork PRs, and that guard is the load-bearing control here rather
than a formality.
