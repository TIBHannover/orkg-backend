package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SmartReviewRepository {
    fun findSmartReviewByResourceId(id: ResourceId): Optional<Resource>
    fun findAllFeaturedSmartReviews(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedSmartReviews(pageable: Pageable): Page<Resource>
    fun findAllUnlistedSmartReviews(pageable: Pageable): Page<Resource>
    fun findAllListedSmartReviews(pageable: Pageable): Page<Resource>
}
