package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.HeadVersion

interface ComparisonRepository {
    fun findVersionHistory(id: ThingId): List<HeadVersion>
}
