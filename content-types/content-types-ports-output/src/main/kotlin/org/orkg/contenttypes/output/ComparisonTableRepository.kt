package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable
import java.util.Optional

interface ComparisonTableRepository {
    fun save(comparisonTable: ComparisonTable)

    fun findByComparisonId(comparisonId: ThingId): Optional<ComparisonTable>

    fun deleteAll()

    fun count(): Long
}
