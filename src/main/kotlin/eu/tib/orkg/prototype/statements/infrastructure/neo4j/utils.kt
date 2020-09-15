package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import java.util.regex.Pattern

val regexCharacters = Pattern.compile("""[<(\[{\\^\-=${'$'}!|\]})?*+.>]""").toRegex()

fun escapeRegexString(str: String) =
    regexCharacters.replace(str) {
            m -> "\\" + m.value
    }

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
