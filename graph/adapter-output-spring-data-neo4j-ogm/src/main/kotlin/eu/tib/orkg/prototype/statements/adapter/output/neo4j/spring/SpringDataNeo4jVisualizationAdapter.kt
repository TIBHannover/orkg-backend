package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jVisualizationRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.VisualizationRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jVisualizationAdapter(
    private val neo4jRepository: Neo4jVisualizationRepository
) : VisualizationRepository {
    override fun findVisualizationByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findVisualizationByResourceId(id.toResourceId()).map { it.toResource() }

    override fun findAllListedVisualizations(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedVisualizations(pageable).map { it.toResource() }

    override fun findAllVisualizationsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllVisualizationsByVisibility(visibility, pageable).map { it.toResource() }
}
