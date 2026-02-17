package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.LegacyNeo4jProblemRepository
import org.orkg.contenttypes.output.LegacyResearchProblemRepository
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class LegacySpringDataNeo4JResearchProblemAdapter(
    private val neo4jRepository: LegacyNeo4jProblemRepository,
) : LegacyResearchProblemRepository {
    override fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByDatasetId(datasetId, pageable).map { it.toResource() }
}
