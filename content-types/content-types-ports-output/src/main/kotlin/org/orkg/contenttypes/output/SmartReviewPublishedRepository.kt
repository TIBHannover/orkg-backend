package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType

interface SmartReviewPublishedRepository {
    fun findById(id: ThingId): Optional<PublishedContentType>
}
