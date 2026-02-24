package org.orkg.contenttypes.output.legacy

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.legacy.LegacyComparisonTable
import java.util.Optional

interface LegacyComparisonTableRepository {
    fun findById(id: ThingId): Optional<LegacyComparisonTable>

    fun save(comparison: LegacyComparisonTable)

    fun update(comparison: LegacyComparisonTable)
}
