package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import java.util.UUID

data class Observatory(
    val id: UUID?,
    var name: String?,
    var description: String?,
    @JsonProperty("research_field")
    var researchField: String?,
    val users: Set<Contributor> = emptySet(),
    @JsonProperty("organization_ids")
    val organizationIds: Set<UUID> = emptySet()
)
