package com.binge.designsystem.component

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.binge.designsystem.motion.NAV_POP_DURATION_MS
import com.binge.designsystem.theme.LocalReduceMotion

/**
 * The animation specs a sheet's content uses, read from the theme's [MotionScheme] and [LocalReduceMotion] in one place so no
 * sheet reads the flag itself. Under reduced motion nothing moves ([spatial] is `snap()`) and a change of state is at most the short
 * fade the navigation transitions use ([NAV_POP_DURATION_MS]); otherwise [spatial] is the scheme's default spatial spring and
 * [fade] its default effects spec.
 *
 * The theme does not set a scheme of its own, so these are Material's default one.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Stable
internal class SheetMotion(
    private val scheme: MotionScheme,
    val reduceMotion: Boolean,
) {
    /** For a change of size or position: the scheme's spatial spring, or a snap under reduced motion. */
    fun <T> spatial(): FiniteAnimationSpec<T> = if (reduceMotion) snap() else scheme.defaultSpatialSpec()

    /** For a change of opacity: the scheme's effects spec, or the short fade of the nav transitions under reduced motion. */
    fun <T> fade(): FiniteAnimationSpec<T> = if (reduceMotion) tween(NAV_POP_DURATION_MS) else scheme.defaultEffectsSpec()
}

/** The [SheetMotion] for the current theme and [LocalReduceMotion]. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun rememberSheetMotion(): SheetMotion {
    val scheme = MaterialTheme.motionScheme
    val reduceMotion = LocalReduceMotion.current
    return remember(scheme, reduceMotion) { SheetMotion(scheme, reduceMotion) }
}
