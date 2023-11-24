package org.orkg.community.domain

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.orkg.auth.domain.User
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId

fun User.toContributor() = Contributor(
    id = ContributorId(this.id),
    name = this.displayName,
    joinedAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC),
    organizationId = this.organizationId?.let { OrganizationId(it) } ?: OrganizationId.createUnknownOrganization(),
    observatoryId = this.observatoryId?.let { ObservatoryId(it) } ?: ObservatoryId.createUnknownObservatory(),
    email = this.email
)
