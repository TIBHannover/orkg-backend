package org.orkg.community.output

import org.orkg.common.ObservatoryId
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ObservatoryResourceRepository {
    fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource>
}
