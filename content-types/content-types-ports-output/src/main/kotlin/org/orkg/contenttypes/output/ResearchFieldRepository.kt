package org.orkg.contenttypes.output

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ResearchFieldRepository {
    fun findById(id: ThingId): Optional<Resource>

    fun findAllPaperCountsPerResearchProblem(fieldId: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem>

    fun findAllContributorIdsIncludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>

    fun findAllContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>

    fun findAllWithBenchmarks(pageable: Pageable): Page<Resource>

    fun findAllListedProblemsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable,
    ): Page<Resource>

    fun findAllProblemsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable,
    ): Page<Resource>
}
