package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jBenchmarkRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jBenchmarkSummary
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.orkg.contenttypes.output.SummarizeBenchmarkQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class BenchmarkAdapter(
    val benchmarkRepository: Neo4jBenchmarkRepository,
) : SummarizeBenchmarkQuery {
    override fun byResearchField(id: ThingId, pageable: Pageable): Page<BenchmarkSummary> =
        benchmarkRepository.summarizeBenchmarkByResearchField(id, pageable)
            .map(Neo4jBenchmarkSummary::toBenchmarkSummary)

    override fun getAll(pageable: Pageable): Page<BenchmarkSummary> =
        benchmarkRepository.summarizeBenchmarkGetAll(pageable).map(Neo4jBenchmarkSummary::toBenchmarkSummary)
}
