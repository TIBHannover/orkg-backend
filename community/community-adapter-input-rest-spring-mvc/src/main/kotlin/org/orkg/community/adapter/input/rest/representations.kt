package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId

data class ObservatoryRepresentation(
    val id: ObservatoryId,
    val name: String,
    val description: String?,
    @get:JsonProperty("research_field")
    val researchField: ResearchFieldRepresentation,
    val members: Set<ContributorId>,
    @get:JsonProperty("organization_ids")
    val organizationIds: Set<OrganizationId>,
    @get:JsonProperty("display_id")
    val displayId: String,
    @get:JsonProperty("sdgs")
    val sustainableDevelopmentGoals: Set<ThingId>
)

data class ResearchFieldRepresentation(
    val id: ThingId?,
    val label: String?
)
