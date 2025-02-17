package org.orkg.contenttypes.input

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperWithStatementCount
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePaperUseCase {
    fun findById(id: ThingId): Optional<Paper>
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
        mentionings: Set<ThingId>? = emptySet(),
    ): Page<Paper>
    fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId>

    fun countAllStatementsAboutPapers(pageable: Pageable): Page<PaperWithStatementCount>

    fun existsByDOI(doi: DOI): Optional<ThingId>

    fun existsByTitle(label: ExactSearchString): Optional<ThingId>
}

interface LegacyRetrievePaperUseCase {
    fun findAllPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
}
