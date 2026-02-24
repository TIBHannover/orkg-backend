package org.orkg.contenttypes.domain.legacy

data class LegacyComparisonConfig(
    val predicates: List<String>,
    val contributions: List<String>,
    val transpose: Boolean,
    val type: LegacyComparisonType,
    val shortCodes: List<String> = emptyList(),
)

enum class LegacyComparisonType {
    PATH,
    MERGE,
}
