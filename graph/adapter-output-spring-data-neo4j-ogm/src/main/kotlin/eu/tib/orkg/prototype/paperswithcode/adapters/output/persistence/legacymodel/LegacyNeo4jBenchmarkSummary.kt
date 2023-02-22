package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.neo4j.annotation.QueryResult

@QueryResult
data class LegacyNeo4jBenchmarkSummary(
    val problem: Neo4jResource,
    val totalPapers: Long,
    val totalDatasets: Long,
    val totalCodes: Long
) {
    fun toBenchmarkSummaryLegacy() =
        BenchmarkSummary(
            ResearchProblem(ThingId(problem.resourceId!!.value), problem.label!!),
            null,
            emptyList(),
            totalPapers.toInt(),
            totalDatasets.toInt(),
            totalCodes.toInt()
        )
}
