package org.orkg.graph.domain

enum class ExtractionMethod {
    AUTOMATIC,
    MANUAL,
    UNKNOWN,
    AI_GENERATED,
    AI_GENERATED_WITH_MANUAL_REVIEW,
    ;

    fun canBeChangedTo(other: ExtractionMethod): Boolean =
        this == other ||
            when (this) {
                AI_GENERATED -> other == AI_GENERATED_WITH_MANUAL_REVIEW
                AI_GENERATED_WITH_MANUAL_REVIEW -> other == AI_GENERATED
                else -> true
            }

    companion object {
        fun parse(value: String): ExtractionMethod =
            try {
                valueOf(value)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid extraction method: $value. Must be one of ${entries.joinToString(", ")}")
            }
    }
}
