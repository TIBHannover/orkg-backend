package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import org.orkg.common.ContributorId

data class ResourceContributor(
    @JsonProperty("created_by")
    val createdBy: ContributorId,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime
)
