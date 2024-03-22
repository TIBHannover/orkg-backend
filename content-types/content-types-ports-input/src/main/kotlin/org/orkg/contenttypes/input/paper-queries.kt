package org.orkg.contenttypes.input

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperWithStatementCount
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePaperUseCase {
    fun findById(id: ThingId): Optional<Paper>
    fun findAll(
        pageable: Pageable,
        label: SearchString?,
        doi: String?,
        visibility: VisibilityFilter?,
        verified: Boolean?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?
    ): Page<Paper>
    fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId>

    fun countAllStatementsAboutPapers(pageable: Pageable): Page<PaperWithStatementCount>
}

interface LegacyRetrievePaperUseCase {
    fun findPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
}
