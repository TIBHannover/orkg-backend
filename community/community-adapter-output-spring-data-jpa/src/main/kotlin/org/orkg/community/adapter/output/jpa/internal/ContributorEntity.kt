package org.orkg.community.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.md5
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.internal.MD5Hash
import org.orkg.eventbus.events.UserRegistered
import org.orkg.eventbus.events.UserRegistered.Role.ADMIN
import org.orkg.eventbus.events.UserRegistered.Role.CURATOR
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "contributors")
class ContributorEntity(
    @Id
    var id: UUID? = null,
    @Column(name = "display_name")
    var displayName: String? = null,
    @Column(name = "joined_at")
    var joinedAt: LocalDateTime? = null,
    @Column(name = "organization_id")
    var organizationId: UUID? = null,
    @Column(name = "observatory_id")
    var observatoryId: UUID? = null,
    @Column(name = "email_md5")
    var emailMD5: String? = null,
    var curator: Boolean? = null,
    var admin: Boolean? = null,
) {
    companion object {
        fun from(event: UserRegistered) = ContributorEntity(
            id = UUID.fromString(event.id),
            displayName = event.displayName,
            joinedAt = event.createdAt,
            organizationId = event.organizationId?.let { UUID.fromString(event.organizationId) },
            observatoryId = event.observatoryId?.let { UUID.fromString(event.observatoryId) },
            emailMD5 = event.email.trim().lowercase().md5,
            curator = CURATOR in event.roles || ADMIN in event.roles,
            admin = ADMIN in event.roles,
        )
    }
}

fun ContributorEntity.toContributor() = Contributor(
    id = ContributorId(id!!),
    name = displayName!!,
    joinedAt = joinedAt!!.atZone(ZoneOffset.UTC).toOffsetDateTime(),
    organizationId = organizationId?.let(::OrganizationId) ?: OrganizationId.UNKNOWN,
    observatoryId = observatoryId?.let(::ObservatoryId) ?: ObservatoryId.UNKNOWN,
    emailMD5 = MD5Hash(emailMD5!!),
    isCurator = curator!!,
    isAdmin = admin!!,
)
