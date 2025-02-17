package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jDatasetSummary
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jDataset
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jDatasetRepository
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.orkg.contenttypes.output.FindDatasetsQuery
import org.orkg.contenttypes.output.SummarizeDatasetQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class DatasetAdapter(
    val datasetRepository: Neo4jDatasetRepository
) : FindDatasetsQuery, SummarizeDatasetQuery {

    override fun findAllDatasetsByResearchProblemId(id: ThingId, pageable: Pageable): Page<Dataset> =
        datasetRepository.findAllDatasetsByResearchProblemId(id, pageable)
            .map(Neo4jDataset::toDataset)

    override fun by(id: ThingId, pageable: Pageable): Page<DatasetSummary> =
        datasetRepository.summarizeDatasetQueryById(id, pageable)
            .map(Neo4jDatasetSummary::toDatasetSummary)

    override fun findAllDatasetSummariesByIdAndResearchProblemId(id: ThingId, problemId: ThingId, pageable: Pageable): Page<DatasetSummary> =
        datasetRepository.findAllDatasetSummariesByIdAndResearchProblemId(id, problemId, pageable)
            .map(Neo4jDatasetSummary::toDatasetSummary)
}
