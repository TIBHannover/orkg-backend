package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import org.orkg.common.ContributorId

data class ClassSubclassRelation(
    val child: Class,
    val parent: Class,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.UNKNOWN
)
