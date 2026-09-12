# Reviewing pull requests

How to review a PR in this repository. `CLAUDE.md` is the source of truth for every rule referenced
here; this is the review-time index into it.

**The bar: green CI is necessary, not sufficient.** CI compiles, tests, formats and lints. It cannot
tell you whether a component belongs here, whether it will break a consumer, or whether it is worth
copying.

## 1. Does this component belong here at all?

The first question, and the one CI cannot ask.

A component here **may not know what Binge's data looks like** — no TMDB types, no account model, no
navigation routes, no provider names. Check the imports, not the intent.

The sharper test: could a companion author with no knowledge of Binge use this and have it mean
something? A component that technically compiles but only makes sense against Binge's domain is one
that should have stayed in Binge.

Watch for the subtle version: a parameter typed generically but *named* after a Binge concept, or a
default value that only makes sense for Binge's data.

## 2. What does this do to the consumers?

**A green build here says nothing about Binge or binge-seerr.** Neither is compiled by this
repository, and both include it directly.

So on any change to a public signature — a parameter added or removed, a type changed, a component
renamed or deleted — check that the PR body says what it means for them. A PR that changes the
surface and claims "no behaviour change" is describing this repository only.

Adding a parameter with a default is usually safe. Reordering parameters is not, and neither is
changing what a default *is*.

## 3. Cross-cutting conventions

Read the diff for these; `build` does not gate them.

- **Dimensions** — every `Dp` from a dimension resource, no inline `.dp` in a composable.
- **Shapes** — a shape token, not a re-derived `RoundedCornerShape`.
- **Strings** — user-visible text as a string resource. Copy taken as a parameter is often the
  better shape here, because the consumer owns the words.
- **Modifier** — a defaulted `Modifier` parameter, applied to the outermost node.
- **Previews** — render with no network and no injected dependency.

## 4. Review adversarially, calibrate the verdict

Skeptical in the hunt, evidence-gated in the verdict.

Try to break it: an edge case that renders wrong rather than crashing, a component that assumes a
size or a locale, a recomposition that will not happen because something is not stable.

But **a hunch is not a finding.** Block only on a concrete, traced problem. Down-rank an
unverifiable hypothesis to a question or a follow-up issue, and be willing to conclude clean. A
false blocking review costs a round-trip and trust; the review is advisory pressure toward
correctness, not a gate.

Defer to documented intent. A trade-off the author flagged and explained is not a defect.

## 5. Do not report a finding as though a gate caught it

The gates are `./gradlew build` and `validateDebugScreenshotTest`. There is no coverage floor and no
custom convention task. Do not write that one failed, and do not ask for one to be added as a
condition of merging — that is an issue, if it is anything. A screenshot failure is a real finding
only when the diff image shows a change the PR did not intend; a frame the PR meant to change wants
its re-recorded baseline in the same commit, not a comment.

## 6. Follow-ups

Scope, not severity. Anything worth doing that reaches beyond this diff is a GitHub issue, filed and
labelled, not a TODO and not a line in the PR description.
