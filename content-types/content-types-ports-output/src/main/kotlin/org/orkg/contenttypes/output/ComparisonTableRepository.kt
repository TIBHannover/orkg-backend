package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable

interface ComparisonTableRepository {
    fun findById(id: ThingId): Optional<ComparisonTable>
    fun save(comparison: ComparisonTable)
    fun update(comparison: ComparisonTable)
}
