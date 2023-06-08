package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jSmartReviewRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import java.util.*
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
