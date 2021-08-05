package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource

data class Neo4jBenchmarkSummary(
    val problem: Neo4jResource,
    val totalPapers: Long,
    val totalDatasets: Long,
    val totalCodes: Long
) {
    fun toBenchmarkSummary() =
        BenchmarkSummary(
            ResearchProblem(problem.resourceId!!, problem.label!!),
            totalPapers.toInt(),
            totalDatasets.toInt(),
            totalCodes.toInt()
        )
}

data class Neo4jDataset(
    @field:Transient
    val dataset: Neo4jResource,
    val totalModels: Long,
    val totalPapers: Long,
    val totalCodes: Long
) {
    fun toDataset() =
        Dataset(
            dataset.resourceId!!,
            dataset.label!!,
            totalModels.toInt(),
            totalPapers.toInt(),
            totalCodes.toInt()
        )
}

data class Neo4jBenchmarkUnpacked(
    val model: String?,
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
            score,
            metric,
            paper.resourceId!!,
            paper.label!!,
            month?.toInt(),
            year?.toInt(),
            codes
        )
}
