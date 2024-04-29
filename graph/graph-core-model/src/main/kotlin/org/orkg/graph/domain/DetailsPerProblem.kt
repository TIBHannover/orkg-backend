package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.collections.List
import org.orkg.common.ThingId

data class DetailsPerProblem(
    val id: ThingId?,
    val label: String?,
    @JsonProperty("created_at")
    val createdAt: String?,
    val featured: Boolean?,
    val unlisted: Boolean?,
    val classes: List<String>,
    @JsonProperty("created_by")
    val createdBy: String?
)
