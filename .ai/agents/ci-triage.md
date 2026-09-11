# CI triage

How CI goes red here, and what the correct fix is. `author-ci-fix` gets **one** repair attempt per
commit, so the point of this table is to spend it on the right thing.

CI is a single job: `./gradlew build --continue`. `--continue` means a red run usually shows more
than one failure — fix them all, or say which you did not.

| Symptom | What it means | Correct fix |
| --- | --- | --- |
| `ktlintMainSourceSetCheck` / `ktlintTestSourceSetCheck` failed | Formatting. Import order, indentation, wrapping, trailing commas. | `./gradlew :designsystem:ktlintFormat`, then read the diff before committing — it reformats, it does not think. **Never** a baseline or a `ktlint-disable` comment. |
| `lintDebug` / `lint-results-*.html` | Android lint. Most often an unused resource, a missing content description, or an API level above `minSdk` 26. | Fix the finding. A `@Suppress` needs a reason in the code, and `lint-baseline.xml` is never the answer. |
| Kotlin compile error | Ordinary. | Fix it. If the fix changes a **public** signature, say so in the PR body — see below. |
| Unit test failure | Ordinary. | Fix the code, not the assertion, unless the assertion is provably the thing that is wrong. |
| `Unresolved reference` after adding a dependency | The catalog and the module's `build.gradle.kts` disagree, or the dependency is not in `libs.versions.toml`. | Add it to the catalog at the root, not as a hardcoded coordinate. |
| Gradle configuration failure mentioning Compose or AGP | A version in `libs.versions.toml` moved. | Check it against Binge's catalog before changing it. These versions are shared. |

## The one that is not in the table

**A green build here does not mean a green build in the consumers.**

This repository cannot compile Binge or binge-seerr. If a repair changes a public signature — a
parameter, a return type, a component's name — it is a change in two other codebases that will not
notice until their next sync.

That is not a reason to avoid the fix. It is a reason to say so, in the PR body, rather than letting
green be read as safe.

## What not to do

- Do not add a baseline, an exclusion, or a suppression to make a gate pass.
- Do not disable a test.
- Do not widen a version range to dodge a conflict.

If the only way to green is through one of those, stop and say so. A stopped attempt with a clear
explanation is worth more than a green build that hid something.
