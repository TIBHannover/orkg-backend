package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import java.util.regex.Pattern

var regexCharacters = Pattern.compile("""[<(\[{\\^\-=${'$'}!|\]})?*+.>]""").toRegex()

fun escapeRegexString(str: String) =
    regexCharacters.replace(str) {
            m -> "\\" + m.value
    }
