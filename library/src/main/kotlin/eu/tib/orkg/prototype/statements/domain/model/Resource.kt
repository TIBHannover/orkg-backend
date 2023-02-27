package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

data class Resource(
    val id: ThingId,
    override val label: String,
    val createdAt: OffsetDateTime,
    val classes: Set<ThingId> = emptySet(),
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    val observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    val featured: Boolean? = null,
    val unlisted: Boolean? = null,
    val verified: Boolean? = null,
) : Thing {
    override val thingId: ThingId = ThingId(id.value)
}
