package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable
import java.util.Optional

interface ComparisonTableRepository {
    fun findById(id: ThingId): Optional<ComparisonTable>

    fun save(comparison: ComparisonTable)

    fun update(comparison: ComparisonTable)
}
