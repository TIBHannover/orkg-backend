package org.orkg.contenttypes.adapter.output.simcomp

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

const val THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE = "thing-id-to-published-literature-list"

@Component
@CacheConfig(cacheNames = [THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE])
class SimCompLiteratureListPublishedAdapter(
    private val repository: SimCompThingRepository
) : LiteratureListPublishedRepository {
    @Cacheable(key = "#id", cacheNames = [THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE])
    override fun findById(id: ThingId): Optional<PublishedContentType> =
        repository.findById(id, ThingType.LIST)
}
