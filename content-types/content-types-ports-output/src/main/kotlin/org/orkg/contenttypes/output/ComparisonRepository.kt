package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ComparisonRepository {
    fun findComparisonByResourceId(id: ThingId): Optional<Resource>
    fun findAllListedComparisons(pageable: Pageable): Page<Resource>
    fun findAllComparisonsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
}
