package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkUnpacked
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.toResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyDatasetAdapter(
    val legacyNeo4jDatasetRepository: LegacyNeo4jDatasetRepository
) : FindDatasetsQuery, SummarizeDatasetQuery {

    override fun forResearchProblem(id: ThingId, pageable: Pageable): Page<Dataset> =
        legacyNeo4jDatasetRepository.findDatasetsByResearchProblem(id.toResourceId(), pageable)
            .map(Neo4jDataset::toDataset)

    override fun by(id: ThingId, pageable: Pageable): Page<DatasetSummary> =
        legacyNeo4jDatasetRepository.summarizeDatasetQueryById(id.toResourceId(), pageable)
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)

    override fun byAndProblem(id: ThingId, problemId: ThingId, pageable: Pageable): Page<DatasetSummary> =
        legacyNeo4jDatasetRepository.summarizeDatasetQueryByIdAndProblemId(
            id.toResourceId(),
            problemId.toResourceId(),
            pageable
        )
            .map(Neo4jBenchmarkUnpacked::toDatasetSummary)
}
