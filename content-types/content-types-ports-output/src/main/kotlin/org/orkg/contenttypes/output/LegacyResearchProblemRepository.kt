package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyResearchProblemRepository {
    fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Resource>
}
