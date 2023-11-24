package org.orkg.contenttypes.adapter.output.neo4j

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jVisualizationRepository
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jVisualizationAdapter(
    private val neo4jRepository: Neo4jVisualizationRepository
) : VisualizationRepository {
    override fun findVisualizationByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findVisualizationByResourceId(id).map { it.toResource() }

    override fun findAllListedVisualizations(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedVisualizations(pageable).map { it.toResource() }

    override fun findAllVisualizationsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllVisualizationsByVisibility(visibility, pageable).map { it.toResource() }
}
