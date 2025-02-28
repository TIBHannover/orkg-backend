package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource

data class Neo4jBenchmarkSummary(
    val problem: Neo4jResource,
    val fields: List<Neo4jResource>,
    val totalPapers: Long,
    val totalDatasets: Long,
    val totalCodes: Long,
) {
    fun toBenchmarkSummary() =
        BenchmarkSummary(
            researchProblem = ResearchProblem(problem.id!!, problem.label!!),
            researchFields = fields.map { ResearchField(it.id!!.value, it.label!!) },
            totalPapers = totalPapers.toInt(),
            totalDatasets = totalDatasets.toInt(),
            totalCodes = totalCodes.toInt()
        )
}

data class Neo4jDataset(
    val dataset: Neo4jResource,
    val totalModels: Long,
    val totalPapers: Long,
    val totalCodes: Long,
) {
    fun toDataset() =
        Dataset(
            id = dataset.id!!,
            label = dataset.label!!,
            totalModels = totalModels.toInt(),
            totalPapers = totalPapers.toInt(),
            totalCodes = totalCodes.toInt()
        )
}

data class Neo4jDatasetSummary(
    val model: String?,
    val modelId: ThingId?,
    val score: String,
    val metric: String,
    val paper: Neo4jResource,
    val codes: List<String>,
    val month: String?,
    val year: String?,
) {
    fun toDatasetSummary() =
        DatasetSummary(
            modelName = model,
            modelId = modelId,
            score = score,
            metric = metric,
            paperId = paper.id!!,
            paperTitle = paper.label!!,
            paperMonth = month?.toInt(),
            paperYear = year?.toInt(),
            codeURLs = codes
        )
}
