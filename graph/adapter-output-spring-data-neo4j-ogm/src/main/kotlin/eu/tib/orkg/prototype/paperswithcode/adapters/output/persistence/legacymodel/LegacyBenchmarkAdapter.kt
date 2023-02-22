package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j.LegacyNeo4jBenchmarkRepository
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j.LegacyNeo4jBenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeBenchmarkQuery
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.toResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyBenchmarkAdapter(
    val legacyBenchmarkRepository: LegacyNeo4jBenchmarkRepository,
) : SummarizeBenchmarkQuery {
    override fun byResearchField(id: ThingId): List<BenchmarkSummary> =
        legacyBenchmarkRepository.summarizeBenchmarkByResearchField(id.toResourceId())
            .map(LegacyNeo4jBenchmarkSummary::toBenchmarkSummaryLegacy)

    override fun getAll(): List<BenchmarkSummary> =
        error("This method is not supported in the PwC legacy model! Calling it is a bug!")
}
