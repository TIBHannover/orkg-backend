package org.orkg.contenttypes.input

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContentType
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveContentTypeUseCase {
    fun findAll(
        pageable: Pageable,
        classes: Set<ContentTypeClass> = ContentTypeClass.entries.toSet(),
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        sustainableDevelopmentGoal: ThingId? = null
    ): Page<ContentType>
}
