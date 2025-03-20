package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContentType
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime

interface ContentTypeUseCases : RetrieveContentTypeUseCase

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
        sustainableDevelopmentGoal: ThingId? = null,
        authorId: ThingId? = null,
        authorName: String? = null,
    ): Page<ContentType>

    fun findAllAsResource(
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
        sustainableDevelopmentGoal: ThingId? = null,
        authorId: ThingId? = null,
        authorName: String? = null,
    ): Page<Resource>
}
