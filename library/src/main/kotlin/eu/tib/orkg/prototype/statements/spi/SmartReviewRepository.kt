package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SmartReviewRepository {
    fun findSmartReviewByResourceId(id: ThingId): Optional<Resource>
    fun findAllListedSmartReviews(pageable: Pageable): Page<Resource>
    fun findAllSmartReviewsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
}
