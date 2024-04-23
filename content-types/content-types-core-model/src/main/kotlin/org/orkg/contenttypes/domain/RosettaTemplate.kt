package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Visibility

data class RosettaTemplate(
    val id: ThingId,
    val label: String,
    val description: String?,
    val formattedLabel: FormattedLabel?,
    val targetClass: ThingId,
    val properties: List<TemplateProperty>,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null
)
