package org.orkg.eventbus.events

import java.time.LocalDateTime
import org.orkg.eventbus.Event

/**
 * An integration event describing that a user registered.
 */
data class UserRegistered(
    val id: String,
    val displayName: String,
    val enabled: Boolean,
    val email: String,
    val roles: Set<String>,
    val createdAt: LocalDateTime,
    val observatoryId: String?,
    val organizationId: String?,
) : Event
