package org.orkg.contenttypes.domain.legacy

import org.orkg.common.ThingId

data class LegacyComparisonTable(
    val id: ThingId,
    val config: LegacyComparisonConfig,
    val data: LegacyComparisonData,
) {
    companion object {
        fun empty(id: ThingId): LegacyComparisonTable = LegacyComparisonTable(
            id = id,
            config = LegacyComparisonConfig(
                predicates = emptyList(),
                contributions = emptyList(),
                transpose = false,
                type = LegacyComparisonType.PATH,
                shortCodes = emptyList(),
            ),
            data = LegacyComparisonData(
                contributions = emptyList(),
                predicates = emptyList(),
                data = emptyMap(),
            ),
        )
    }
}
