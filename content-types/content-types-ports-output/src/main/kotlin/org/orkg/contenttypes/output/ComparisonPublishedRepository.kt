package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedComparison

interface ComparisonPublishedRepository {
    fun findById(id: ThingId): Optional<PublishedComparison>
    fun save(comparison: PublishedComparison)
}
