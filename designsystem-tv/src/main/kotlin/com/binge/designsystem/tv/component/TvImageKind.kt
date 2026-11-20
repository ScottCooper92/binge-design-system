package com.binge.designsystem.tv.component

/**
 * What an image slot stands in for, which decides its glyph — see [TvImagePlaceholder].
 *
 * It says what a slot stands in for, not what a cell opens, which is why it has `Episode` and `Photo`.
 */
enum class TvImageKind { Movie, TvShow, Person, Episode, Photo }
