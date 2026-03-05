package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import java.time.OffsetDateTime

data class ResourceContributor(
    @field:JsonProperty("created_by")
    val createdBy: ContributorId,
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)
