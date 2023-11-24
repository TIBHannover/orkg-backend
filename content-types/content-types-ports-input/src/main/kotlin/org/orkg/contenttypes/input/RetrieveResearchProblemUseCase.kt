package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.DetailsPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchProblemUseCase : GetProblemFlagQuery, LoadProblemPort {
    fun findById(id: ThingId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ThingId): List<FieldWithFreq>

    fun findAllEntitiesBasedOnClassByProblem(
        problemId: ThingId,
        classes: List<String>,
        visibilityFilter: VisibilityFilter,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    fun findTopResearchProblems(): List<Resource>

    fun findContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem>

    fun forDataset(id: ThingId, pageable: Pageable): Optional<Page<ResearchProblem>>

}
