package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.neo4j.annotation.QueryResult

@QueryResult
data class Neo4jBenchmarkSummary(
    val problem: Neo4jResource,
    val fields: List<Neo4jResource>,
    val totalPapers: Long,
    val totalDatasets: Long,
    val totalCodes: Long
) {
    fun toBenchmarkSummary() =
        BenchmarkSummary(
            ResearchProblem(problem.id!!, problem.label!!),
            fields.map { ResearchField(it.id!!.value, it.label!!) },
            totalPapers.toInt(),
            totalDatasets.toInt(),
            totalCodes.toInt()
        )
}

@QueryResult
data class Neo4jDataset(
    val dataset: Neo4jResource,
    val totalModels: Long,
    val totalPapers: Long,
    val totalCodes: Long
) {
    fun toDataset() =
        Dataset(
            dataset.id!!,
            dataset.label!!,
            totalModels.toInt(),
            totalPapers.toInt(),
            totalCodes.toInt()
        )
}

@QueryResult
data class Neo4jBenchmarkUnpacked(
    val model: String?,
    val modelId: String?,
    val score: String,
    val metric: String,
    val paper: Neo4jResource,
    val codes: List<String>,
    val month: String?,
    val year: String?
) {
    // FIXME: conform the naming of the method and returning type
    fun toDatasetSummary() =
        DatasetSummary(
            model,
            modelId?.let(::ThingId),
            score,
            metric,
            paper.id!!,
            paper.label!!,
            month?.toInt(),
            year?.toInt(),
            codes
        )
}
