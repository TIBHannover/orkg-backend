package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jComparisonRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ComparisonRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jComparisonAdapter(
    private val neo4jRepository: Neo4jComparisonRepository,
) : ComparisonRepository {

    override fun findComparisonByResourceId(id: ThingId): Optional<Resource> =
        neo4jRepository.findComparisonByResourceId(id.toResourceId()).map { it.toResource() }

    override fun findAllFeaturedComparisons(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllFeaturedComparisons(pageable).map { it.toResource() }

    override fun findAllNonFeaturedComparisons(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllNonFeaturedComparisons(pageable).map { it.toResource() }

    override fun findAllUnlistedComparisons(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllUnlistedComparisons(pageable).map { it.toResource() }

    override fun findAllListedComparisons(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedComparisons(pageable).map { it.toResource() }
}
