package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType
import java.util.Optional

interface SmartReviewPublishedRepository {
    fun findById(id: ThingId): Optional<PublishedContentType>

    fun save(smartReview: PublishedContentType)
}
