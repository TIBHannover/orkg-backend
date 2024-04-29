package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import org.orkg.common.ContributorId

abstract class StatementMixin(
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @field:JsonProperty("created_by")
    val createdBy: ContributorId
)
