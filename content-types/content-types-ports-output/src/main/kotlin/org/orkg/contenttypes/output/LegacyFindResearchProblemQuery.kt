package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyFindResearchProblemQuery {
    fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<ResearchProblem>
}
