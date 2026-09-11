# CLAUDE.md

Conventions for this repository. The agent workflows in `.github/workflows/` read this file from
`main` and treat it as the source of truth; so should you.

## What this repository is

The design system shared by Binge and the companion apps. Read `README.md` for why it exists and
what belongs in it.

Status is pre-alpha. The carve from Binge has not happened — what is here is the build, the bots and
a placeholder.

## The one rule everything else serves

**A component here may not know what Binge's data looks like.**

No TMDB types, no account model, no navigation routes, no provider names. A component takes what it
renders as parameters and gives back callbacks. If it needs to name one of Binge's concepts to do
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

## Gates

CI runs `./gradlew build`, which is the Kotlin compile, the unit tests, `ktlintCheck` and Android
lint — the ktlint plugin and AGP both wire themselves into `check`, and `build` depends on it.

That is the whole gate. There is no screenshot suite, no coverage floor and no custom convention
task in this repository yet, so do not look for one and do not report a finding as though one had
caught it.

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
