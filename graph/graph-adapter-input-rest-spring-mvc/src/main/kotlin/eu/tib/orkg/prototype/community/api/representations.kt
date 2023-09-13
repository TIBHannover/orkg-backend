package eu.tib.orkg.prototype.community

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface ObservatoryRepresentation {
    val id: ObservatoryId
    val name: String
    val description: String?
    @get:JsonProperty("research_field")
    val researchField: ResearchFieldRepresentation
    val members: Set<ContributorId>
    @get:JsonProperty("organization_ids")
    val organizationIds: Set<OrganizationId>
    @get:JsonProperty("display_id")
    val displayId: String
}

interface ResearchFieldRepresentation {
    val id: ThingId?
    val label: String?
}
