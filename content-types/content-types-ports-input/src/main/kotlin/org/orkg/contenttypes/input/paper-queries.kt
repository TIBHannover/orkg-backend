package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Paper
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrievePaperUseCase {
    fun findById(id: ThingId): Optional<Paper>
    fun findAll(pageable: Pageable): Page<Paper>
    fun findAllByDOI(doi: String, pageable: Pageable): Page<Paper>
    fun findAllByTitle(title: String, pageable: Pageable): Page<Paper>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Paper>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Paper>
    fun findAllByResearchFieldAndVisibility(
        researchFieldId: ThingId,
        visibility: VisibilityFilter,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Paper>
    fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId>
}

interface LegacyRetrievePaperUseCase {
    fun findPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
}
