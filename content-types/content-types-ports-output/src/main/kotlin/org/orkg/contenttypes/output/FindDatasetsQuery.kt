package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Dataset
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindDatasetsQuery {
    fun findAllDatasetsByResearchProblemId(id: ThingId, pageable: Pageable): Page<Dataset>
}
