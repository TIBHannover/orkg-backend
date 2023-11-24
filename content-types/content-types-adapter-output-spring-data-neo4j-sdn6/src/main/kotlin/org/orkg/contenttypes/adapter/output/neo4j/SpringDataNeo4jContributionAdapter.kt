package org.orkg.contenttypes.adapter.output.neo4j

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jContributionRepository
import org.orkg.contenttypes.output.ContributionRepository
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jContributionAdapter(
    private val neo4jRepository: Neo4jContributionRepository
) : ContributionRepository {

    override fun findContributionByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findContributionByResourceId(id).map { it.toResource() }

    override fun findAllListedContributions(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedContributions(pageable).map { it.toResource() }

    override fun findAllContributionsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllContributionsByVisibility(visibility, pageable).map { it.toResource() }
}
