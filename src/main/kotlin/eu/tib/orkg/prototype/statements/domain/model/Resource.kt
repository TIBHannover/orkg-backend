package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class Resource(
    val id: ResourceId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    val classes: Set<ClassId> = emptySet(),
    val shared: Int = 0
) : Thing()
