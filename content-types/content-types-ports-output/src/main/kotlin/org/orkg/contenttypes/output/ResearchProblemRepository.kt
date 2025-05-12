package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchProblemRepository {
    fun findAllResearchFieldsWithPaperCountByProblemId(problemId: ThingId): Iterable<FieldWithFreq>

    fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): Page<ContributorPerProblem>

    fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Resource>
}
