package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId

data class Resource(
    override val id: ThingId,
    override val label: String,
    override val createdAt: OffsetDateTime,
    val classes: Set<ThingId> = emptySet(),
    override val createdBy: ContributorId = ContributorId.UNKNOWN,
    val observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    val organizationId: OrganizationId = OrganizationId.UNKNOWN,
    val visibility: Visibility = Visibility.DEFAULT,
    val verified: Boolean? = null,
    val unlistedBy: ContributorId? = null,
    override val modifiable: Boolean = true
) : Thing {
    val publishableClasses: Set<ThingId>
        get() = classes intersect PUBLISHABLE_CLASSES

    fun hasPublishableClasses(): Boolean = publishableClasses.isNotEmpty()

    fun isOwnedBy(contributorId: ContributorId): Boolean = createdBy == contributorId
}
