package org.orkg.contenttypes.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId

// Are of class "Dataset" in the database
data class Dataset(
    val id: ThingId,
    val label: String, // Name of the resource
    override val totalModels: Int,
    override val totalPapers: Int,
    override val totalCodes: Int,
) : PaperTotal,
    CodeTotal,
    ModelTotal

data class DatasetSummary(
    @JsonProperty("model_name")
    val modelName: String?,
    @JsonProperty("model_id")
    val modelId: ThingId?,
    val score: String,
    val metric: String,
    @JsonProperty("paper_id")
    val paperId: ThingId,
    @JsonProperty("paper_title")
    val paperTitle: String,
    @JsonProperty("paper_month")
    val paperMonth: Int?,
    @JsonProperty("paper_year")
    val paperYear: Int?,
    @JsonProperty("code_urls")
    val codeURLs: List<String>,
)
