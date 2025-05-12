package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.contenttypes.output.FindResearchProblemQuery
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class ResearchProblemService(
    private val researchProblemRepository: ResearchProblemRepository,
    private val researchProblemQueries: FindResearchProblemQuery,
) : ResearchProblemUseCases {
    override fun findAllResearchFields(problemId: ThingId): List<FieldWithFreq> =
        researchProblemRepository.findAllResearchFieldsWithPaperCountByProblemId(problemId)
            .map { FieldWithFreq(it.field, it.freq) }

    override fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem> =
        researchProblemRepository.findAllContributorsPerProblem(problemId, pageable).content

    override fun findAllByDatasetId(id: ThingId, pageable: Pageable): Page<ResearchProblem> =
        researchProblemQueries.findAllByDatasetId(id, pageable)
}
