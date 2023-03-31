package eu.tib.orkg.prototype.auth.domain

import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.auth.spi.RoleRepository
import eu.tib.orkg.prototype.auth.spi.UserRepository
import java.time.LocalDateTime
import java.util.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository
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
            createdAt = LocalDateTime.now(),
            observatoryId = null,
            organizationId = null,
        )
        repository.save(newUser)
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
    }

    override fun updateRole(userId: UUID) {
        var user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        val role =
            roleRepository.findByName("ORGANIZATION_OWNER").orElseThrow { IllegalStateException("Role missing.") }
        user = user.copy(roles = user.roles + role)
        repository.save(user)
    }

    override fun addUserObservatory(
        observatoryId: UUID,
        organizationId: UUID,
        contributor: User
    ): User {
        // FIXME: check if user exists?
        val user = contributor.copy(
            observatoryId = observatoryId,
            organizationId = organizationId,
        )
        repository.save(user)
        return user
    }

    override fun updateOrganizationAndObservatory(
        userId: UUID,
        organizationId: UUID?,
        observatoryId: UUID?
    ) {
        var user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        user = user.copy(organizationId = organizationId, observatoryId = observatoryId)
        repository.save(user)
    }

    override fun deleteUserObservatory(contributor: UUID) {
        var user = repository.findById(contributor).orElseThrow { UserNotFound(contributor) }
        user = user.copy(observatoryId = null, organizationId = null)
        repository.save(user)
    }
}
