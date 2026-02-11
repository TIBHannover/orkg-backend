package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.adapter.output.simcomp.mapping.PublishedContentTypeRepresentationAdapter
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

const val THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE = "thing-id-to-published-literature-list"

@Component
@CacheConfig(cacheNames = [THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE])
class SimCompLiteratureListPublishedAdapter(
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
    private val objectMapper: ObjectMapper,
    private val repository: SimCompThingRepository,
) : LiteratureListPublishedRepository,
    PublishedContentTypeRepresentationAdapter {
    @Cacheable(key = "#id", cacheNames = [THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE])
    override fun findById(id: ThingId): Optional<PublishedContentType> =
        repository.findById(id, ThingType.LIST).map { it.toPublishedContentType(objectMapper) }

    override fun save(literatureList: PublishedContentType) {
        repository.save(
            id = literatureList.id,
            type = ThingType.LIST,
            data = literatureList.toPublishedContentTypeRepresentation(MediaTypeCapabilities.EMPTY)
        )
    }
}
