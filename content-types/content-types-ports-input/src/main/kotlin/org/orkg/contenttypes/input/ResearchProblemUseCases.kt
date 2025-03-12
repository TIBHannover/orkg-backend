package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.DetailsPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ResearchProblemUseCases : RetrieveResearchProblemUseCase

interface RetrieveResearchProblemUseCase {
    fun findById(id: ThingId): Optional<Resource>

    fun findAllResearchFields(problemId: ThingId): List<FieldWithFreq>

    fun findAllEntitiesBasedOnClassByProblem(
        problemId: ThingId,
        classes: List<String>,
        visibilityFilter: VisibilityFilter,
        pageable: Pageable,
    ): Page<DetailsPerProblem>

    fun findTopResearchProblems(): List<Resource>

    fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem>

    fun findAllByDatasetId(id: ThingId, pageable: Pageable): Page<ResearchProblem>
}
