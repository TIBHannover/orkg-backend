package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface DatasetUseCases : RetrieveDatasetUseCase

interface RetrieveDatasetUseCase {
    fun findAllDatasetsByResearchProblemId(id: ThingId, pageable: Pageable): Optional<Page<Dataset>>

    fun findAllDatasetSummariesByIdAndResearchProblemId(id: ThingId, problemId: ThingId, pageable: Pageable): Optional<Page<DatasetSummary>>
}
