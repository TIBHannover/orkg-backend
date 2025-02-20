package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class ComparisonTable(
    val id: ThingId,
    val config: ComparisonConfig,
    val data: ComparisonData,
) {
    companion object {
        fun empty(id: ThingId): ComparisonTable = ComparisonTable(
            id = id,
            config = ComparisonConfig(
                predicates = emptyList(),
                contributions = emptyList(),
                transpose = false,
                type = ComparisonType.PATH,
                shortCodes = emptyList()
            ),
            data = ComparisonData(
                contributions = emptyList(),
                predicates = emptyList(),
                data = emptyMap()
            )
        )
    }
}
