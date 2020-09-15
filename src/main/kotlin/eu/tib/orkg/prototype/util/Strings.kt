package eu.tib.orkg.prototype.util

import java.util.regex.Pattern

/**
 * A class that escapes all character in a String that have a special meaning in Regular Expressions.
 */
class EscapedRegex(private val regex: String) {
    private object RegExChars {
        val pattern = Pattern.compile("""[<(\[{\\^\-=${'$'}!|\]})?*+.>]""").toRegex()
    }

    override fun toString(): String {
        return RegExChars.pattern.replace(regex) { match ->
            "\\" + match.value
        }
    }
}

/**
 * Trims a string and replaces multiple whitespace characters with a single space character.
 */
class SanitizedWhitespace(private val input: String) {
    override fun toString() = input.trim().replace("""\s+""".toRegex(), " ")
}
