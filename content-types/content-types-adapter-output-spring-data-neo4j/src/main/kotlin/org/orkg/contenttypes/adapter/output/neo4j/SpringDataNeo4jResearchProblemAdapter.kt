package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jProblemRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jResearchFieldWithPaperCount
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jResearchProblemAdapter(
    private val neo4jRepository: Neo4jProblemRepository,
) : ResearchProblemRepository {
    override fun findAllResearchFieldsWithPaperCountByProblemId(problemId: ThingId): Iterable<FieldWithFreq> =
        neo4jRepository.findAllResearchFieldsWithPaperCountByProblemId(problemId).map { it.toFieldWithFreq() }

    override fun findAllContributorsPerProblem(
        problemId: ThingId,
        pageable: Pageable,
    ): Page<ContributorPerProblem> =
        neo4jRepository.findAllContributorsPerProblem(problemId, pageable)

    override fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByDatasetId(datasetId, pageable).map { it.toResource() }

    fun Neo4jResearchFieldWithPaperCount.toFieldWithFreq() =
        FieldWithFreq(
            field = field.toResource(),
            freq = paperCount
        )
}
