package eu.tib.orkg.prototype.community.domain.model

import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun User.toContributor() = Contributor(
    id = ContributorId(this.id),
    name = this.displayName,
    joinedAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC),
    organizationId = this.organizationId?.let { OrganizationId(it) } ?: OrganizationId.createUnknownOrganization(),
    observatoryId = this.observatoryId?.let { ObservatoryId(it) } ?: ObservatoryId.createUnknownObservatory(),
    email = this.email
)
