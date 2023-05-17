package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkUnpacked
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDataset
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDatasetRepository
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class DatasetAdapter(
    val datasetRepository: Neo4jDatasetRepository
) : FindDatasetsQuery, SummarizeDatasetQuery {

    override fun forResearchProblem(id: ThingId, pageable: Pageable): Page<Dataset> =
        datasetRepository.findDatasetsByResearchProblem(id, pageable)
            .map(Neo4jDataset::toDataset)

    override fun by(id: ThingId, pageable: Pageable): Page<DatasetSummary> =
        datasetRepository.summarizeDatasetQueryById(id, pageable)
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)

    override fun byAndProblem(id: ThingId, problemId: ThingId, pageable: Pageable): Page<DatasetSummary> =
        datasetRepository.summarizeDatasetQueryByIdAndProblemId(id, problemId, pageable)
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)
}
