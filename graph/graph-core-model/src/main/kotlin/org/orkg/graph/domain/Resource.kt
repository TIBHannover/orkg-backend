package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId

data class Resource(
    override val id: ThingId,
    override val label: String,
    val createdAt: OffsetDateTime,
    val classes: Set<ThingId> = emptySet(),
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    val observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    val visibility: Visibility = Visibility.DEFAULT,
    val verified: Boolean? = null,
    val unlistedBy: ContributorId? = null
) : Thing {
    val publishableClasses: Set<ThingId>
        get() = classes intersect PUBLISHABLE_CLASSES

    fun hasPublishableClasses(): Boolean = publishableClasses.isNotEmpty()
}