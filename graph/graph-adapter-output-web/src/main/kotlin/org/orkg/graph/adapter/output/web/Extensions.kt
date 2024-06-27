package org.orkg.graph.adapter.output.web

import java.util.regex.Pattern

internal fun Pattern.matchSingleGroupOrNull(input: String): String? {
    val matcher = matcher(input)
    if (matcher.matches() && matcher.groupCount() == 1) {
        return matcher.group(1)
    }
    return null
}
