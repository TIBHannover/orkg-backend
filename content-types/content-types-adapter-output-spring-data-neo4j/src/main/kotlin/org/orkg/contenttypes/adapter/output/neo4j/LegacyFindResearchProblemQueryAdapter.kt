package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.output.LegacyFindResearchProblemQuery
import org.orkg.contenttypes.output.LegacyResearchProblemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class LegacyFindResearchProblemQueryAdapter(
    private val legacyResearchProblemRepository: LegacyResearchProblemRepository,
) : LegacyFindResearchProblemQuery {
    override fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<ResearchProblem> =
        legacyResearchProblemRepository.findAllByDatasetId(datasetId, pageable)
            .map { ResearchProblem(it.id, it.label) }
}
