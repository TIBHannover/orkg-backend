package org.orkg.contenttypes.adapter.output.neo4j

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonRepository
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jComparisonAdapter(
    private val neo4jRepository: Neo4jComparisonRepository,
) : ComparisonRepository {

    override fun findComparisonByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findComparisonByResourceId(id).map { it.toResource() }

    override fun findAllListedComparisons(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedComparisons(pageable).map { it.toResource() }

    override fun findAllComparisonsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllComparisonsByVisibility(visibility, pageable).map { it.toResource() }
}
