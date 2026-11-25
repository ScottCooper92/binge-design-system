package com.binge.designsystem.component

/**
 * Whether a line that ends between the code points [before] and [after] has split a word: neither is a space, the break is not after a
 * hyphen, slash or dash, and neither side is a character of a script written without spaces (CJK, Thai, Lao, Khmer, Myanmar), beside
 * which the line breaker may break and which no Latin word can be split by.
 *
 * The one rule behind [WholeWordText], which falls back to one line when its layout broke a word, and behind the test fixture
 * `assertNoTextClipped`, which fails a layout that did. They share it so a label one wraps cannot fail the other.
 */
internal fun isBrokenWord(before: Int, after: Int): Boolean =
    !Character.isWhitespace(before) &&
        !Character.isWhitespace(after) &&
        before !in BREAK_WELCOME_AFTER &&
        !(before.isUnspaced() || after.isUnspaced())

/**
 * Whether a code point is in a script written without spaces. The script alone misses marks that belong to no script, such as the
 * prolonged sound mark `ー` and the ideographic comma and full stop, which a line may end after, so the Unicode blocks count too.
 */
private fun Int.isUnspaced(): Boolean =
    Character.UnicodeScript.of(this) in UNSPACED_SCRIPTS || Character.UnicodeBlock.of(this) in CJK_BLOCKS

private val UNSPACED_SCRIPTS =
    setOf(
        Character.UnicodeScript.HAN,
        Character.UnicodeScript.HIRAGANA,
        Character.UnicodeScript.KATAKANA,
        Character.UnicodeScript.HANGUL,
        Character.UnicodeScript.THAI,
        Character.UnicodeScript.LAO,
        Character.UnicodeScript.KHMER,
        Character.UnicodeScript.MYANMAR,
    )

private val CJK_BLOCKS =
    setOf(
        Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
        Character.UnicodeBlock.HIRAGANA,
        Character.UnicodeBlock.KATAKANA,
        Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS,
        Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS,
        Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS,
    )

/** A hyphen, a slash, an en dash, an em dash and the Unicode hyphen: a line may end after one without breaking a word. */
private val BREAK_WELCOME_AFTER = setOf('-'.code, '/'.code, 0x2013, 0x2014, 0x2010)
