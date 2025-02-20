package org.orkg.contenttypes.domain

data class ComparisonConfig(
    val predicates: List<String>,
    val contributions: List<String>,
    val transpose: Boolean,
    val type: ComparisonType,
    val shortCodes: List<String> = emptyList(),
)

enum class ComparisonType {
    PATH,
    MERGE,
}
