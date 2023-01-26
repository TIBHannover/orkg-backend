package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jSmartReviewRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jSmartReviewAdapter(
    private val neo4jRepository: Neo4jSmartReviewRepository
) : SmartReviewRepository {
    override fun findSmartReviewByResourceId(id: ResourceId): Optional<Resource> =
        neo4jRepository.findSmartReviewByResourceId(id).map { it.toResource() }

    override fun findAllFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllFeaturedSmartReviews(pageable).map { it.toResource() }

    override fun findAllNonFeaturedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllNonFeaturedSmartReviews(pageable).map { it.toResource() }

    override fun findAllUnlistedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllUnlistedSmartReviews(pageable).map { it.toResource() }

    override fun findAllListedSmartReviews(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedSmartReviews(pageable).map { it.toResource() }
}
