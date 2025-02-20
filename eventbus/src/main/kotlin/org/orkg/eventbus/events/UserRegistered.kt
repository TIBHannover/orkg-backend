package org.orkg.eventbus.events

import org.orkg.eventbus.Event
import java.time.LocalDateTime

/**
 * An integration event describing that a user registered.
 */
data class UserRegistered(
    val id: String,
    val displayName: String,
    val enabled: Boolean,
    val email: String,
    val roles: Set<Role>,
    val createdAt: LocalDateTime,
    val observatoryId: String?,
    val organizationId: String?,
) : Event {
    enum class Role {
        CURATOR,
        ADMIN,
        ;

        companion object {
            fun from(s: String): Role = entries.find { it.name == s }
                ?: throw IllegalArgumentException("Unable to map role $s. Should be one of ${entries.sorted()}.")
        }
    }
}
