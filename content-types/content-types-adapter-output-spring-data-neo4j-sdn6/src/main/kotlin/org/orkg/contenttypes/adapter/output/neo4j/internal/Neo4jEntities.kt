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
