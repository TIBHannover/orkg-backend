package org.orkg.contenttypes.adapter.output.neo4j

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jSmartReviewRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jSmartReviewAdapter(
    private val neo4jRepository: Neo4jSmartReviewRepository
) : SmartReviewRepository {
    override fun findSmartReviewByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findSmartReviewByResourceId(id).map { it.toResource() }

    override fun findAllListedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedSmartReviews(pageable).map { it.toResource() }

    override fun findAllSmartReviewsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllSmartReviewsByVisibility(visibility, pageable).map { it.toResource() }
}
