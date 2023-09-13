package eu.tib.orkg.prototype.community.testing.fixtures

import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

fun createContributor(
    id: ContributorId = ContributorId(UUID.randomUUID()),
    name: String = "Some Name",
    email: String? = null,
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    clock: Clock = Clock.systemDefaultZone(),
    joinedAt: OffsetDateTime = OffsetDateTime.now(clock),
): Contributor = Contributor(
    id = id,
    name = name,
    joinedAt = joinedAt,
    observatoryId = observatoryId,
    organizationId = organizationId,
    email = email,
)
