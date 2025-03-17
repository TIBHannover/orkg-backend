package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.DatasetRepresentation
import org.orkg.contenttypes.adapter.input.rest.DatasetSummaryRepresentation
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.springframework.data.domain.Page

interface DatasetRepresentationAdapter {
    fun Page<Dataset>.mapToDatasetRepresentation(): Page<DatasetRepresentation> =
        map { it.toDatasetRepresentation() }

    fun Dataset.toDatasetRepresentation(): DatasetRepresentation =
        DatasetRepresentation(id, label, totalModels, totalPapers, totalCodes)

    fun Page<DatasetSummary>.mapToDatasetSummaryRepresentation(): Page<DatasetSummaryRepresentation> =
        map { it.toDatasetSummaryRepresentation() }

    fun DatasetSummary.toDatasetSummaryRepresentation(): DatasetSummaryRepresentation =
        DatasetSummaryRepresentation(modelName, modelId, score, metric, paperId, paperTitle, paperMonth, paperYear, codeURLs)
}
