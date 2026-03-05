package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.graph.domain.PredicatePath
import java.time.OffsetDateTime

data class ObservatoryRepresentation(
    val id: ObservatoryId,
    val name: String,
    val description: String?,
    @field:JsonProperty("research_field")
    val researchField: ResearchFieldRepresentation,
    val members: Set<ContributorId>,
    @field:JsonProperty("organization_ids")
    val organizationIds: Set<OrganizationId>,
    @field:JsonProperty("display_id")
    val displayId: String,
    @field:JsonProperty("sdgs")
    val sustainableDevelopmentGoals: Set<ThingId>,
)

data class ResearchFieldRepresentation(
    val id: ThingId?,
    val label: String?,
)

data class ObservatoryFilterRepresentation(
    val id: ObservatoryFilterId,
    @field:JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    val label: String,
    @field:JsonProperty("created_by")
    val createdBy: ContributorId,
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    val path: PredicatePath,
    val range: ThingId,
    val exact: Boolean,
    val featured: Boolean,
)

data class ContributorIdentifierRepresentation(
    val type: ContributorIdentifier.Type,
    val value: String,
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)
