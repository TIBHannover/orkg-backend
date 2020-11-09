package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class Observatory(
    val id: UUID?,
    val name: String?,
    val description: String?,
    @JsonProperty("research_field")
    val researchField: String?,
    val members: Set<UUID> = emptySet(),
    @JsonProperty("organization_ids")
    val organizationIds: Set<UUID> = emptySet()
)
