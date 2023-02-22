package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j.LegacyNeo4jDatasetRepository
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkUnpacked
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.toResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyDatasetAdapter(
    val legacyNeo4jDatasetRepository: LegacyNeo4jDatasetRepository
) : FindDatasetsQuery, SummarizeDatasetQuery {

    override fun forResearchProblem(id: ThingId): List<Dataset> =
        legacyNeo4jDatasetRepository.findDatasetsByResearchProblem(id.toResourceId()).map(Neo4jDataset::toDataset)

    override fun by(id: ThingId): List<DatasetSummary> =
        legacyNeo4jDatasetRepository.summarizeDatasetQueryById(id.toResourceId()).map(Neo4jBenchmarkUnpacked::toDatasetSummary)

    override fun byAndProblem(id: ThingId, problemId: ThingId): List<DatasetSummary> =
        legacyNeo4jDatasetRepository.summarizeDatasetQueryByIdAndProblemId(id.toResourceId(), problemId.toResourceId())
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)
}
