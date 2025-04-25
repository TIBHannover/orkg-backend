package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.graph.domain.PredicatePath
import java.time.OffsetDateTime

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
    val sustainableDevelopmentGoals: Set<ThingId>,
)

data class ResearchFieldRepresentation(
    val id: ThingId?,
    val label: String?,
)

data class ObservatoryFilterRepresentation(
    val id: ObservatoryFilterId,
    @get:JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    val label: String,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    val path: PredicatePath,
    val range: ThingId,
    val exact: Boolean,
    val featured: Boolean,
)
