package org.orkg.auth.domain

import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.RoleRepository
import org.orkg.auth.output.UserRepository
import org.orkg.common.md5
import org.orkg.eventbus.EventBus
import org.orkg.eventbus.events.DisplayNameUpdated
import org.orkg.eventbus.events.UserRegistered
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository,
    private val clock: Clock,
    private val eventBus: EventBus,
) : AuthUseCase {
    override fun findByEmail(email: String): Optional<User> {
        return repository.findByEmailIgnoreCase(email)
    }

    override fun findById(id: UUID): Optional<User> = repository.findById(id)

    override fun registerUser(anEmail: String, aPassword: String, aDisplayName: String?, userId: UUID?): UUID {
        val id = userId ?: UUID.randomUUID()
        val role = roleRepository.findByName("ROLE_USER").map<Set<Role>>(::setOf).orElseGet(::emptySet)
        val newUser = User(
            id = id,
            email = anEmail.lowercase(),
            password = passwordEncoder.encode(aPassword),
            displayName = aDisplayName ?: "Anonymous user",
            enabled = true,
            roles = role,
            createdAt = LocalDateTime.now(clock),
            observatoryId = null,
            organizationId = null,
        )
        repository.save(newUser)
        eventBus.post(
            UserRegistered(
                id = newUser.id.toString(),
                displayName = newUser.displayName,
                enabled = newUser.enabled,
                email = newUser.email.md5,
                roles = newUser.roles.map(Role::name).toSet(),
                createdAt = newUser.createdAt,
                observatoryId = newUser.observatoryId?.toString(),
                organizationId = newUser.organizationId?.toString(),
            )
        )
        return id
    }

    override fun checkPassword(userId: UUID, aPassword: String): Boolean {
        val user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        return passwordEncoder.matches(aPassword, user.password)
    }

    override fun updatePassword(userId: UUID, aPassword: String) {
        var user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        user = user.copy(password = passwordEncoder.encode(aPassword))
        repository.save(user)
    }

    override fun updateName(userId: UUID, newName: String) {
        var user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        user = user.copy(displayName = newName)
        repository.save(user)
        eventBus.post(DisplayNameUpdated(user.id, newName))
    }
}
