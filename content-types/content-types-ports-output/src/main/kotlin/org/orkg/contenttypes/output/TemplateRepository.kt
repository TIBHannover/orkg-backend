package org.orkg.contenttypes.output

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TemplateRepository {
    fun findAll(
        searchString: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        researchFieldId: ThingId? = null,
        researchProblemId: ThingId? = null,
        targetClassId: ThingId? = null,
        pageable: Pageable
    ): Page<Resource>
}
