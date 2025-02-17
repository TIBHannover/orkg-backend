package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.DatasetSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SummarizeDatasetQuery {
    fun findAllDatasetSummariesByIdAndResearchProblemId(id: ThingId, problemId: ThingId, pageable: Pageable): Page<DatasetSummary>
}
