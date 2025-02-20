package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId
import kotlin.collections.List

data class DetailsPerProblem(
    val id: ThingId?,
    val label: String?,
    @JsonProperty("created_at")
    val createdAt: String?,
    val featured: Boolean?,
    val unlisted: Boolean?,
    val classes: List<String>,
    @JsonProperty("created_by")
    val createdBy: String?,
)
