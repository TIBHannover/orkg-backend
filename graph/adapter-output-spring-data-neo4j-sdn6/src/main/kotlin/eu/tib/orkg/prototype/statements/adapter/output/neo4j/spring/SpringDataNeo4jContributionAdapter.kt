package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jContributionRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ContributionRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jContributionAdapter(
    private val neo4jRepository: Neo4jContributionRepository
) : ContributionRepository {

    override fun findContributionByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findContributionByResourceId(id.toResourceId()).map { it.toResource() }

    override fun findAllListedContributions(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedContributions(pageable).map { it.toResource() }

    override fun findAllContributionsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllContributionsByVisibility(visibility, pageable).map { it.toResource() }
}
