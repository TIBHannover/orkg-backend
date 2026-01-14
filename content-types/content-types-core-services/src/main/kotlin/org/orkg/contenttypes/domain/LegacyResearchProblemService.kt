package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LegacyResearchProblemUseCases
import org.orkg.contenttypes.output.LegacyFindResearchProblemQuery
import org.orkg.contenttypes.output.LegacyResearchProblemRepository
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class LegacyResearchProblemService(
    private val legacyResearchProblemRepository: LegacyResearchProblemRepository,
    private val researchProblemQueries: LegacyFindResearchProblemQuery,
) : LegacyResearchProblemUseCases {
    override fun findAllResearchFields(problemId: ThingId): List<FieldWithFreq> =
        legacyResearchProblemRepository.findAllResearchFieldsWithPaperCountByProblemId(problemId)
            .map { FieldWithFreq(it.field, it.freq) }

    override fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem> =
        legacyResearchProblemRepository.findAllContributorsPerProblem(problemId, pageable).content

    override fun findAllByDatasetId(id: ThingId, pageable: Pageable): Page<ResearchProblem> =
        researchProblemQueries.findAllByDatasetId(id, pageable)
}
