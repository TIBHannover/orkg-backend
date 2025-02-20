package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import java.time.OffsetDateTime

data class ResourceContributor(
    @JsonProperty("created_by")
    val createdBy: ContributorId,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)
