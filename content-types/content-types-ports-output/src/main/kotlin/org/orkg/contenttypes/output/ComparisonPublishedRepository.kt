package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedComparison
import java.util.Optional

interface ComparisonPublishedRepository {
    fun findById(id: ThingId): Optional<PublishedComparison>

    fun save(comparison: PublishedComparison)
}
