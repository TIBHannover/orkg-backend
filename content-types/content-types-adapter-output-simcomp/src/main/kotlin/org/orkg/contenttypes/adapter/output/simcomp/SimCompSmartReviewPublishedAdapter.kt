package org.orkg.contenttypes.adapter.output.simcomp

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

const val THING_ID_TO_PUBLISHED_SMART_REVIEW_CACHE = "thing-id-to-published-smart-review"

@Component
@CacheConfig(cacheNames = [THING_ID_TO_PUBLISHED_SMART_REVIEW_CACHE])
class SimCompSmartReviewPublishedAdapter(
    private val repository: SimCompThingRepository
) : SmartReviewPublishedRepository {
    @Cacheable(key = "#id", cacheNames = [THING_ID_TO_PUBLISHED_SMART_REVIEW_CACHE])
    override fun findById(id: ThingId): Optional<PublishedContentType> =
        repository.findById(id, ThingType.REVIEW)
}
