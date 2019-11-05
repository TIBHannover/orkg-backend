package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class Literal(
    val id: LiteralId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?
)
