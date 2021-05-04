package eu.tib.orkg.prototype.util

import java.util.regex.Pattern

/**
 * Interface to make string helper classes composable.
 */
interface StringHelper {
    override fun toString(): String
}

/**
 * A class that escapes all character in a String that have a special meaning in Regular Expressions.
 */
class EscapedRegex(private val regex: String) : StringHelper {
    private object RegExChars {
        val pattern = Pattern.compile("""[<(\[{\\^\-=${'$'}!|\]})?*+.>]""").toRegex()
    }

    constructor(helper: StringHelper) : this(helper.toString())

    override fun toString(): String {
        return RegExChars.pattern.replace(regex) { match ->
            "\\" + match.value
        }
    }
}

/**
 * Trims a string and replaces multiple whitespace characters with a single space character.
 */
class SanitizedWhitespace(private val input: String) : StringHelper {
    constructor(helper: StringHelper) : this(helper.toString())

    override fun toString() = input.trim().replace("""\s+""".toRegex(), " ")
}

/**
 * Transforms all spaces in string to a Regular Expression pattern matching multiple whitespaces.
 *
 * Spaces will be transformed individually.
 * The [SanitizedWhitespace] class can be used to sanitize the string beforehand.
 * Other whitespace will be left untouched.
 */
class WhitespaceIgnorantPattern(private val input: String) : StringHelper {
    constructor(helper: StringHelper) : this(helper.toString())

    override fun toString(): String {
        return input.replace(" ", "\\s+")
    }
}

fun replaceWhitespaceWithUnderscores(input: String): String = input.replace(" ", "_")

fun removeSingleQuotes(input: String): String = input.replace("'", "")
