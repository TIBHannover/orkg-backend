package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkUnpacked
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDataset
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDatasetRepository
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class DatasetAdapter(
    val datasetRepository: Neo4jDatasetRepository
) : FindDatasetsQuery, SummarizeDatasetQuery {

    override fun forResearchProblem(id: ResourceId): List<Dataset> =
        datasetRepository.findDatasetsByResearchProblem(id)
            .map(Neo4jDataset::toDataset)

    override fun by(id: ResourceId): List<DatasetSummary> =
        datasetRepository.summarizeDatasetQueryById(id)
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)

    override fun byAndProblem(id: ResourceId, problemId: ResourceId): List<DatasetSummary> =
        datasetRepository.summarizeDatasetQueryByIdAndProblemId(id, problemId)
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)
}
