package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import java.time.OffsetDateTime

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
) : Thing