package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyResearchFieldUseCases : LegacyRetrieveResearchFieldUseCase

interface LegacyRetrieveResearchFieldUseCase {
    fun findAllPaperCountsPerResearchProblem(id: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem>

    fun findAllContributorsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>

    fun findAllContributorsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor>

    fun findAllResearchProblemsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean = false,
        pageable: Pageable,
    ): Page<Resource>

    fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField>
}
