package org.orkg.auth.domain

import java.time.LocalDateTime
import java.util.*

data class User(
    val id: UUID,
    val email: String,
    val password: String,
    val displayName: String,
    val enabled: Boolean = false,
    val createdAt: LocalDateTime,
    val roles: Set<Role>,
    val organizationId: UUID?,
    val observatoryId: UUID?,
) {
    val isCurator: Boolean = Role("ROLE_ADMIN") in roles // FIXME: should be its own status
    val isAdmin: Boolean = Role("ROLE_ADMIN") in roles
}
