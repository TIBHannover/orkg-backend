package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchProblemUseCases : RetrieveResearchProblemUseCase

interface RetrieveResearchProblemUseCase {
    fun findAllResearchFields(problemId: ThingId): List<FieldWithFreq>

    fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem>

    fun findAllByDatasetId(id: ThingId, pageable: Pageable): Page<ResearchProblem>
}
