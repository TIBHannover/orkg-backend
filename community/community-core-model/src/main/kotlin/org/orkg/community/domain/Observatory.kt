package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId

data class Observatory(
    val id: ObservatoryId,
    val name: String,
    val description: String?,
    val researchField: ThingId?,
    val members: Set<ContributorId> = emptySet(),
    val organizationIds: Set<OrganizationId> = emptySet(),
    val displayId: String
)
