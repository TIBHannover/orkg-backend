package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j.LegacyNeo4jDatasetRepository
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkUnpacked
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyDatasetAdapter(
    val legacyNeo4jDatasetRepository: LegacyNeo4jDatasetRepository
) : FindDatasetsQuery, SummarizeDatasetQuery {

    override fun forResearchProblem(id: ResourceId): List<Dataset> =
        legacyNeo4jDatasetRepository.findDatasetsByResearchProblem(id).map(Neo4jDataset::toDataset)

    override fun by(id: ResourceId): List<DatasetSummary> =
        legacyNeo4jDatasetRepository.summarizeDatasetQueryById(id).map(Neo4jBenchmarkUnpacked::toDatasetSummary)

    override fun byAndProblem(id: ResourceId, problemId: ResourceId): List<DatasetSummary> =
        legacyNeo4jDatasetRepository.summarizeDatasetQueryByIdAndProblemId(id, problemId)
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)
}
