package org.orkg.graph.domain

enum class ExtractionMethod {
    AUTOMATIC,
    MANUAL,
    UNKNOWN,
    ;

    companion object {
        fun parse(value: String): ExtractionMethod =
            try {
                valueOf(value)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid extraction method: $value. Must be one of ${entries.joinToString(", ")}")
            }
    }
}
