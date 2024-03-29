package org.orkg.auth.testing.fixtures

import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import org.orkg.auth.domain.Role
import org.orkg.auth.domain.User
import org.orkg.testing.fixedClock

fun createUser(id: UUID = UUID.fromString("ee06bdf3-d6f3-41d1-8af2-64c583d9057e"), clock: Clock = fixedClock) = User(
    id = id,
    email = "user@example.org",
    password = "secret",
    displayName = "Example User",
    enabled = true,
    createdAt = LocalDateTime.now(clock),
    organizationId = null,
    observatoryId = null,
    roles = emptySet(),
)

fun createAdminUser(id: UUID) = createUser(id).copy(roles = setOf(Role("ROLE_ADMIN")))
