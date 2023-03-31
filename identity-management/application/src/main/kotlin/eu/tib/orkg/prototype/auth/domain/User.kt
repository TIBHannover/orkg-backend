package eu.tib.orkg.prototype.auth.domain

import eu.tib.orkg.prototype.auth.spi.ObservatoryId
import eu.tib.orkg.prototype.auth.spi.OrganizationId
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
    val organizationId: OrganizationId?,
    val observatoryId: ObservatoryId?,
)
