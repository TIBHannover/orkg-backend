package eu.tib.orkg.prototype.community.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId

data class Observatory(
    val id: ObservatoryId?,
    val name: String?,
    val description: String?,
    @JsonProperty("research_field")
    val researchField: ResearchField?,
    val members: Set<Contributor> = emptySet(),
    @JsonProperty("organization_ids")
    val organizationIds: Set<OrganizationId> = emptySet(),
    @JsonProperty("display_id")
    val displayId: String? = null
)

data class ResearchField(var id: String?, var label: String?)
