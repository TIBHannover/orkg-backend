package org.orkg.export.domain

internal fun escapeLiterals(literal: String): String {
    return literal
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("(\\r|\\n|\\r\\n)+".toRegex(), "\\\\n")
}
