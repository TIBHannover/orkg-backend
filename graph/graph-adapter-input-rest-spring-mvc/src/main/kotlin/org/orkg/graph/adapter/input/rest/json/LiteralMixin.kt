package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import java.time.OffsetDateTime

abstract class LiteralMixin(
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @field:JsonProperty("created_by")
    val createdBy: ContributorId,
)
