#!/usr/bin/env bash
# Prints the Gradle arguments that validate (or re-record) one shard of this repository's screenshot tests.
#
#   scripts/screenshot-shard.sh <validate|update> <shard> <count>
#
# Shard numbers run from 1 to <count>. Every `@PreviewTest` class of every module is listed by its fully qualified
# name, the names are sorted, and shard N takes every <count>th one starting at the Nth. The selection depends only on
# the files in the checkout, so CI and a maintainer re-recording a shard locally get the same classes, and `validate`
# and `update` always agree on them.
#
# The output is one `:module:<mode>DebugScreenshotTest --tests <class> --tests <class> ...` group per module that has a
# class in the shard. A module with none is left out, because Gradle fails a task whose `--tests` filter matches nothing.
# A shard with no class at all prints nothing, and the caller should skip it.
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo "usage: $0 <validate|update> <shard 1..count> <count>" >&2
  exit 2
fi
mode="$1"
shard="$2"
count="$3"
case "$mode" in
  validate | update) ;;
  *) echo "mode must be validate or update, not '$mode'" >&2; exit 2 ;;
esac
case "$shard$count" in
  '' | *[!0-9]*) echo "shard and count must be positive numbers" >&2; exit 2 ;;
esac
if [ "$shard" -lt 1 ] || [ "$shard" -gt "$count" ]; then
  echo "shard must be between 1 and $count" >&2
  exit 2
fi

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

# One line per class: "<module> <fully.qualified.Class>". A class is a top-level `class` in a file under
# src/screenshotTest that holds a @PreviewTest function; its package comes from the file's `package` line.
list_classes() {
  local file module package
  while IFS= read -r file; do
    grep -q '@PreviewTest' "$file" || continue
    module="${file%%/src/*}"
    package="$(sed -n 's/^package \(.*\)$/\1/p' "$file" | head -n 1)"
    sed -n 's/^\(internal \|public \)\{0,1\}class \([A-Za-z0-9_]*\).*/\2/p' "$file" |
      while IFS= read -r name; do
        echo "$module $package.$name"
      done
  done < <(find . -path '*/src/screenshotTest/*' -name '*.kt' -not -path '*/build/*' | sed 's|^\./||' | LC_ALL=C sort)
}

selected="$(list_classes | LC_ALL=C sort -k2 | awk -v shard="$shard" -v count="$count" '(NR - 1) % count == shard - 1')"
[ -n "$selected" ] || exit 0

for module in $(echo "$selected" | cut -d' ' -f1 | LC_ALL=C sort -u); do
  printf ':%s:%sDebugScreenshotTest' "$module" "$mode"
  echo "$selected" | awk -v module="$module" '$1 == module { printf " --tests %s", $2 }'
  printf ' '
done
echo
