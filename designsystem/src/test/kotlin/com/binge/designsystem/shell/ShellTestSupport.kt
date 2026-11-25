package com.binge.designsystem.shell

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/** A route compared by [name] only, so a second instance with the same name is equal but not the same. */
internal data class Route(
    val name: String,
) : NavKey

internal fun stackOf(vararg names: String): NavBackStack<NavKey> = NavBackStack<NavKey>(*names.map { Route(it) }.toTypedArray())

internal fun NavBackStack<NavKey>.names(): List<String> = map { (it as Route).name }
