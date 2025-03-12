package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.DatasetUseCases
import org.orkg.contenttypes.output.FindDatasetsQuery
import org.orkg.contenttypes.output.SummarizeDatasetQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class DatasetService(
    private val findDatasets: FindDatasetsQuery,
    private val summarizeDataset: SummarizeDatasetQuery,
) : DatasetUseCases {
    override fun findAllDatasetsByResearchProblemId(id: ThingId, pageable: Pageable): Page<Dataset> =
        findDatasets.findAllDatasetsByResearchProblemId(id, pageable)

    override fun findAllDatasetSummariesByIdAndResearchProblemId(
        id: ThingId,
        problemId: ThingId,
        pageable: Pageable,
    ): Page<DatasetSummary> =
        summarizeDataset.findAllDatasetSummariesByIdAndResearchProblemId(id, problemId, pageable)
}
