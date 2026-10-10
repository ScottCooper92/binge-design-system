---
name: audit
description: Whole-repo (or module-scoped) sweep of binge-design-system for components that know too much about Binge, missing catalog samples and screenshot frames, resource-discipline violations, Material 3 / tv-material mixing, dead tokens and doc drift — the invariants CI does NOT gate, with the repo's exemption rules baked in so findings arrive pre-filtered, verified, and filed as issues. Use when asked to audit the design system, hunt for drift or dead tokens, or health-check a module. For a diff, use /code-review instead — this skill is not diff-scoped.
---

# Design system audit

CI runs `./gradlew build validateDebugScreenshotTest`: compile, unit tests, `ktlintCheck`, Android lint
and `checkTvMaterialSeparation` (wired into `designsystem-tv`'s `check`), plus screenshot validation
against the committed PNGs. There is **no** coverage floor, detekt, baseline or custom convention task
here — do not look for one and do not report a finding as though one had caught it. The audit's value
is what those don't decide: whether a component belongs here at all, whether every component has its
catalog fixture and frame, and what the two consumers (Binge, binge-seerr) will trip over.

Scope to both modules or one: `designsystem/` (`com.binge.designsystem`) and `designsystem-tv/`
(`com.binge.designsystem.tv`). Skip `build/`.

**Three disciplines make this useful rather than noisy:**

1. **Exemptions first.** Check every hit against the exemption for that rule. Recurring ones:
   `src/test`, `src/screenshotTest`, `src/testFixtures`, `@Preview` bodies and preview/catalog
   fixture data (literal dp and copy are fine there), the `BingeShapes` tokens (`RoundedCornerShape(n.dp)`
   is sanctioned because they are consumed outside composition), and the colour-scheme adapter in
   `BingeTvTokens.kt` (the one sanctioned M3 ↔ tv-material crossing).
2. **Verify before reporting.** Re-read every candidate in context; label it **confirmed**
   (`file:line` plus the exact violation) or **plausible** (needs a design decision). Never report a
   raw grep hit.
3. **Sanity-check the hunt.** Run each grep against a known positive before trusting a zero. Search by
   construction site, not by bare name. Screenshot-framed components, `@Preview`s, resources referenced
   from another module or from a consumer, and `internal` symbols used across the two modules are real
   references a grep-only sweep calls dead.

## Dimension 1 — does it belong here? (the one rule)

A component here **may not know what Binge's data looks like** — no TMDB types, no account model, no
routes, no provider names, no Binge formatter. Check imports and *names*, not intent.

- Imports of `com.binge.core.*`, `com.binge.feature.*`, `com.cooper.binge.*`, TMDB/Retrofit/Room types,
  navigation route classes. Any hit is confirmed.
- The subtle version: a parameter typed generically but **named** for a Binge concept
  (`tmdbId`, `mediaType`, `watchlist…`), a default value that only makes sense for Binge's data, a KDoc
  that describes a Binge screen, a string resource that names TMDB / a provider / Binge features.
- Copy that is baked in where the consumer should own the words (`CLAUDE.md`: a component taking its
  copy as a parameter is usually the better shape). Judge per component; report only clear cases.
- The test: could a companion author with no knowledge of Binge use this and have it mean something?
- `DisplayFormatters.kt` must stay display-only (`formatRating`, `formatVoteCount`, `toInitials`,
  `badgeCountLabel`, the relative-date formatter); flag any formatter that reads a Binge type.

## Dimension 2 — Compose conventions (`CLAUDE.md` > Compose conventions)

- **Inline dp**: `grep -rnE '[0-9]+\.dp' --include='*.kt' designsystem*/src/main` — exempt `@Preview`,
  catalog/preview fixture data, `BingeShapes.kt`, and `0.dp` floor idioms. Every other Dp comes from
  `dimensionResource`.
- **Hardcoded user copy**: `Text("…")`, string literals in `contentDescription`, display strings in
  Kotlin constants/enums. Strings live in this module's `strings.xml` **with a `values-es` twin**
  (`ls designsystem/src/main/res/values-es`); check the Spanish exists for every string
  `checkTranslation…`-style lint would want, and that pluralised strings carry CLDR forms Spanish needs.
- **Shapes**: `RoundedCornerShape(` outside `BingeShapes.kt` where a token fits.
- **Hardcoded colours / theme values**: `Color(0x`, `Color.` outside the theme files; use tokens.
- **`Modifier` parameter**: a public composable without a defaulted `modifier: Modifier = Modifier`
  applied to its outermost node (first param after required ones, per Compose API guidelines); a modifier
  applied to an inner node instead.
- **Previews** must render with no network or injected dependency; `@Preview(name = …)` tokens short,
  lowercase, space-free (they are baked into baseline filenames — Windows `MAX_PATH`). Check the names.
- **Insets**: the preview renderer reports every `WindowInsets.*` as 0dp; a component reserving an inset
  needs a frame that would change if the reservation went.
- Public API hygiene: a `public` composable/type that only the module itself uses should be `internal`
  (a narrower surface is a smaller consumer-break risk); the reverse — `internal` symbols a consumer
  reaches via reflection or copy — is a finding.

## Dimension 3 — catalog + screenshot coverage (the CI blind spot)

`validateDebugScreenshotTest` guards only what has a frame; it can report a *changed* frame but never
an absent one.

- **Every public component has a `…Sample()` in `catalog/`** and a `@PreviewTest` frame that renders
  it (`CLAUDE.md`). Enumerate public `@Composable`s in `component/`, `layout/`, `nav/`, `focus/` (TV)
  and diff against `catalog/*Samples.kt` and `src/screenshotTest/**`. Report components with no sample,
  samples with no frame, and frames that render something other than the sample (a second hand-rolled
  baseline for one state).
- **Variant coverage**: for each enum/sealed type a component `when`s over, and each branch that renders
  differently (a chip tone, an empty/error layout, a size, a disabled state), is there a frame? Every
  unrendered case is zero regression coverage.
- **TV focused state**: a TV component taking `isFocused: Boolean` renders a distinct focused
  appearance; it needs a frame in that state (focus is a parameter so it is screenshot-testable).
- **Orphaned baselines**: PNGs under `src/screenshotTestDebug/reference/` for frames that no longer
  exist (`git ls-files` vs frame names); previews rendering blank content.
- Locale pinning: frames must not render pseudolocale (accented/bracketed) strings.

## Dimension 4 — Material 3 vs tv-material separation

`checkTvMaterialSeparation` scans imports; audit what an import scan cannot see.

- Any `androidx.tv.material3` symbol reachable from `designsystem/` (phone module) or
  `androidx.compose.material3.*` in `designsystem-tv/` beyond the sanctioned adapter in
  `BingeTvTokens.kt`. Check the task's own pattern still fires (run it against a known positive — it
  can silently rot the way any grep gate does).
- Type-name collisions: `MaterialTheme`/`Text`/`Button`/`Card`/`Surface`/`Icon` resolved through a
  wildcard, alias or same-package symbol to the wrong tree.
- TV components taking M3 types in their public signatures (`ColorScheme`, `Shapes`, `Typography`).
- Focus rules: `requestFocus()` outside the sanctioned focus package or `runCatching`; raw
  `focusRestorer` / `focusProperties { onEnter/onExit }` outside the shared focus units; modifier
  orderings the focus docs call fatal (`tvFocusTarget` before `.clickable`, `focusRequester` after
  `focusGroup`); a navigation/selection committed unconditionally inside `onFocusChanged`.

## Dimension 5 — consumer impact and API drift

A green build here does not mean a green build in Binge or binge-seerr. `consumer-check` compiles binge-seerr's
main and unit-test sources against a PR's head, informationally; Binge is private and is not compiled.

- Compare the public surface against what the consumers use: look in the sibling checkouts
  (`../Binge`, `../binge-seerr`) for imports of `com.binge.designsystem` symbols that no longer exist,
  changed signatures, renamed resources (`R.string.*`, `R.dimen.*`) or removed catalog samples. Report
  drift with the consumer file and line.
- Resource names consumers reference via `DesR.dimen.*` / `DesR.string.*` that are missing or renamed.
- `libs.versions.toml`: Compose BOM, AGP, Kotlin, JDK toolchain and compileSdk must agree with the
  consumers' catalogs (`CLAUDE.md`: the version catalog is not a local decision); flag divergence.
- Theme surface: `BingeExpressiveTheme`/`BingeTvTheme` parameters (brand, dynamic colour) — binge-seerr
  passes its own `BingeBrand`; confirm nothing hardcodes Binge's amber where the brand should flow.

## Dimension 6 — dead, unreachable & orphaned code

Nothing here gates unused symbols, and Android lint's `UnusedResources` cannot see a resource used only
by a consumer, so **default to reporting "referenced by no code in either repo" not "unused here"**.
Check `../Binge` and `../binge-seerr` before calling anything dead.

- **Unreferenced value resources** (`dimen`/`string`/`plurals`/`color`/`integer`/`bool`): 1 occurrence
  repo-wide (the declaration) is unused *in this repo*; then grep the consumers. Check every
  `values-*` qualifier folder under each module's `src/main/res` (list them; do not work from memory)
  for overrides of anything flagged, and for overrides without a base entry.
- **Unreferenced drawables** (0 occurrences of the filename); exempt manifest/adaptive-icon references.
- **Catalog samples** with no frame; **public symbols** no consumer imports (KDoc claiming a consumer
  that does not exist is worth more than the line it occupies); dead enum entries / sealed subtypes.
- Constants whose KDoc describes an abandoned mechanism (check the doc against the implementation).

## Dimension 7 — doc-vs-code drift

Extract every checkable claim from `README.md` (the tree under "What is here", the "111 files / 40
components" style counts), `CLAUDE.md` (gate list, module list, "three/four slices", no-coverage-floor
claims), `docs/`, `.ai/agents/*.md` and `.github/workflows/*` and verify each against the source, the
filesystem and `build.gradle.kts`. Report claim → reality → which side should change. Status claims
("pre-alpha, consumed by both") and the list of what still lives in Binge rot fastest.

## Output contract

- Rank by severity; **confirmed** before **plausible**; every finding grounded at `file:line` with the
  rule or the consumer-facing failure stated.
- Substantive findings → GitHub Issues on this repo, deduped against the open backlog first
  (`search_issues`), using labels the repository already has, grouped by theme (a scattering of dead
  tokens is one issue). Scope, not severity, decides issue vs fix-in-this-PR (`CLAUDE.md` > Follow-ups).
  Micro-nits → a short list in chat. Never a `// TODO` comment.
- A finding that needs a change in Binge or binge-seerr is filed **there**, linked from here.
- State what was **not** audited. A silent partial audit reads as a clean bill; "checked, clean" per
  dimension is a valid result.

## Scaling

- **Quick** (default): greps + spot verification, one pass.
- **Thorough**: parallel read-only subagents per dimension (or per module), each told the exemptions
  above, then an adversarial verification pass (two skeptics per finding, default-refute) before
  anything is filed. This spends real tokens — do it only when the user opts in ("use a workflow").
- **Re-runs**: diff against the previous audit's issues — annotate or reopen rather than re-file.
