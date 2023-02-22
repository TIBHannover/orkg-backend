package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkRepository
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeBenchmarkQuery
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.toResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
class BenchmarkAdapter(
    val benchmarkRepository: Neo4jBenchmarkRepository,
) : SummarizeBenchmarkQuery {
    override fun byResearchField(id: ThingId): List<BenchmarkSummary> =
        benchmarkRepository.summarizeBenchmarkByResearchField(id.toResourceId())
            .map(Neo4jBenchmarkSummary::toBenchmarkSummary)

    override fun getAll(): List<BenchmarkSummary> =
        benchmarkRepository.summarizeBenchmarkGetAll().map(Neo4jBenchmarkSummary::toBenchmarkSummary)
}
