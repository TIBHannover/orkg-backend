package org.orkg.auth.domain

import java.time.LocalDateTime
import java.util.*

typealias UserId = UUID
typealias ObservatoryId = UUID
typealias OrganizationId = UUID

data class User(
    val id: UUID,
    val email: String,
    val password: String,
    val displayName: String,
    val enabled: Boolean = false,
    val createdAt: LocalDateTime,
    val roles: Set<Role>,
    val organizationId: OrganizationId?,
    val observatoryId: ObservatoryId?,
) {
    val isCurator: Boolean = Role("ROLE_ADMIN") in roles // FIXME: should be its own status
    val isAdmin: Boolean = Role("ROLE_ADMIN") in roles
}
