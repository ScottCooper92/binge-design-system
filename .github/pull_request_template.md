## What and why

<!-- One short paragraph, then `Closes #N` (or `Part of #N`). Say what a change to a public signature means for consumers. -->

## Checked before pushing

<!-- Tick what you ran locally on the final head. If you couldn't run one (it runs out of memory, or needs the network), leave it unticked and say so. Don't tick a box you didn't run. -->
- [ ] `./gradlew ktlintCheck lint checkTvMaterialSeparation`
- [ ] The unit tests of every module this touches
- [ ] `validateDebugScreenshotTest` for every class this touches; new or changed frames viewed
- [ ] A new public component has a `…Sample()` in `catalog/` and a `@PreviewTest` frame that renders it
- [ ] Consumer impact stated above: additive, a deprecated forward, or a break that Binge, binge-seerr, binge-archive and binge-jellyfin must follow
- [ ] Left to CI, and why: <!-- e.g. the full validateDebugScreenshotTest runs out of memory locally -->
