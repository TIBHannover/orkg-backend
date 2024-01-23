package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonVersion

interface ComparisonRepository {
    fun findVersionHistory(id: ThingId): List<ComparisonVersion>
}
