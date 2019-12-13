package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

data class Class(
    val id: ClassId?,
    val label: String,
    val uri: URI?,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0)
)
