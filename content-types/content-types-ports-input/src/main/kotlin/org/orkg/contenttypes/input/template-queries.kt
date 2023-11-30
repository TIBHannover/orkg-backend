package org.orkg.contenttypes.input

import org.orkg.contenttypes.domain.Template
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveTemplateUseCase {
    fun findById(id: ThingId): Optional<Template>
    fun findAll(
        searchString: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        researchField: ThingId? = null,
        researchProblem: ThingId? = null,
        targetClass: ThingId? = null,
        pageable: Pageable
    ): Page<Template>
}
