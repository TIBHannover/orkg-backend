package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkRepository
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeBenchmarkQuery
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.toResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class BenchmarkAdapter(
    val benchmarkRepository: Neo4jBenchmarkRepository,
) : SummarizeBenchmarkQuery {
    override fun byResearchField(id: ThingId, pageable: Pageable): Page<BenchmarkSummary> =
        benchmarkRepository.summarizeBenchmarkByResearchField(id.toResourceId(), pageable)
            .map(Neo4jBenchmarkSummary::toBenchmarkSummary)

    override fun getAll(pageable: Pageable): Page<BenchmarkSummary> =
        benchmarkRepository.summarizeBenchmarkGetAll(pageable).map(Neo4jBenchmarkSummary::toBenchmarkSummary)
}
