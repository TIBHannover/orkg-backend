package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class Dataset(
    val id: ThingId,
    val label: String,
    val totalModels: Int,
    val totalPapers: Int,
    val totalCodes: Int,
)

data class DatasetSummary(
    val modelName: String?,
    val modelId: ThingId?,
    val score: String,
    val metric: String,
    val paperId: ThingId,
    val paperTitle: String,
    val paperMonth: Int?,
    val paperYear: Int?,
    val codeURLs: List<String>,
)
