package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SmartReviewRepository {
    fun findSmartReviewByResourceId(id: ThingId): Optional<Resource>
    fun findAllListedSmartReviews(pageable: Pageable): Page<Resource>
    fun findAllSmartReviewsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
}
