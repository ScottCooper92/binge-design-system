package com.binge.designsystem.tv.template

/** Which end of a range the scale is setting. */
enum class TvRangeBound { Min, Max }

/** One step on a range's scale: the key it is stored as, and how it reads. */
class TvRangeStep(
    val key: String,
    val label: String,
)

/**
 * The range once [picked] is set as [bound]; a null [picked] is "Any" and clears that bound. A pick that would cross the other
 * bound clears the other, so the range never inverts. Bounds are ordered by [rank], which is the step's place on the scale
 * unless the scale's keys carry a value of their own (a rating's number), so a stored bound on no step, such as a floor
 * of 7.5 on a whole-number scale, still orders correctly. Without such a rank an off-scale bound sorts before every step.
 */
internal fun rangeAfterPick(
    steps: List<TvRangeStep>,
    bound: TvRangeBound,
    from: String?,
    to: String?,
    picked: String?,
    rank: (String) -> Float = { key -> steps.indexOfFirst { it.key == key }.toFloat() },
): Pair<String?, String?> {
    val rankOf = { key: String? -> key?.let(rank) ?: Float.NEGATIVE_INFINITY }
    return when (bound) {
        TvRangeBound.Min -> picked to to?.takeIf { picked == null || rankOf(it) >= rankOf(picked) }
        TvRangeBound.Max -> from?.takeIf { picked == null || rankOf(it) <= rankOf(picked) } to picked
    }
}
