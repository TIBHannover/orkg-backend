package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.output.FindResearchProblemQuery
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class FindResearchProblemQueryAdapter(
    private val researchProblemRepository: ResearchProblemRepository,
) : FindResearchProblemQuery {
    override fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<ResearchProblem> =
        researchProblemRepository.findAllByDatasetId(datasetId, pageable)
            .map { ResearchProblem(it.id, it.label) }
}
