package eu.tib.orkg.prototype.community.domain.model

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

data class Observatory(
    val id: ObservatoryId,
    val name: String,
    val description: String?,
    val researchField: ThingId?,
    val members: Set<ContributorId> = emptySet(),
    val organizationIds: Set<OrganizationId> = emptySet(),
    val displayId: String
)
