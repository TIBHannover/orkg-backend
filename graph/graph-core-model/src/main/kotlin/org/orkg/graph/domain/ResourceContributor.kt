package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class ResourceContributor(
    @JsonProperty("created_by")
    val createdBy: String, // FIXME: This should be ContributorId
    @JsonProperty("created_at")
    val createdAt: String // FIXME: This should be OffsetDateTime
)
