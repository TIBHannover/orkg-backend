package org.orkg.contenttypes.output

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PaperRepository {
    fun findAllPapersRelatedToResource(id: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        doi: String? = null,
        doiPrefix: String? = null,
        visibility: VisibilityFilter? = null,
        verified: Boolean? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        sustainableDevelopmentGoal: ThingId? = null,
        mentionings: Set<ThingId>? = null
    ): Page<Resource>
}
