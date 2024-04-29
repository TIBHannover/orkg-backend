package org.orkg.contenttypes.input

import java.time.OffsetDateTime
import org.orkg.contenttypes.domain.Template
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveTemplateUseCase {
    fun findById(id: ThingId): Optional<Template>
    fun findAll(
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        researchProblem: ThingId? = null,
        targetClass: ThingId? = null,
        pageable: Pageable
    ): Page<Template>
}
