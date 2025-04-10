package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.graph.domain.ExtractionMethod
import java.time.OffsetDateTime

abstract class ResourceMixin(
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @field:JsonProperty("created_by")
    val createdBy: ContributorId,
    @field:JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    @field:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    @field:JsonProperty("organization_id")
    val organizationId: OrganizationId,
    @field:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?,
)
