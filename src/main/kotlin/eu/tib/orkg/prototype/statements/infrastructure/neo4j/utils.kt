package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import java.util.regex.Pattern

val regexCharacters = Pattern.compile("""[<(\[{\\^\-=${'$'}!|\]})?*+.>]""").toRegex()

@Deprecated("Not used anymore", ReplaceWith("EscapedRegex(str).toString()", "eu.tib.orkg.prototype.util.EscapedRegex"))
fun escapeRegexString(str: String) =
    regexCharacters.replace(str) {
            m -> "\\" + m.value
    }
