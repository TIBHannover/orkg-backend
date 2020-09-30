package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import java.util.UUID

data class Observatory(
    val id: UUID?,
    val name: String?,
    val description: String?,
    @JsonProperty("research_field")
    val researchField: String?,
    val members: Set<Contributor> = emptySet(),
    @JsonProperty("organization_ids")
    val organizationIds: Set<UUID> = emptySet()
)
