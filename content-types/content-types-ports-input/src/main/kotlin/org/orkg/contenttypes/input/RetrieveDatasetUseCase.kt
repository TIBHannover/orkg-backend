package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveDatasetUseCase {
    fun forResearchProblem(id: ThingId, pageable: Pageable): Optional<Page<Dataset>>
    fun summaryFor(id: ThingId, problemId: ThingId, pageable: Pageable): Optional<Page<DatasetSummary>>
}
