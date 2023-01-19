package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.rest.UserNotFound
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
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
) {
    fun findByEmail(email: String): Optional<UserEntity> {
        return repository.findByEmailIgnoreCase(email)
    }

    fun findById(id: UUID): Optional<UserEntity> = repository.findById(id)

    fun registerUser(anEmail: String, aPassword: String, aDisplayName: String?, userId: UUID? = null): UUID {
        val id = userId ?: UUID.randomUUID()
        val role = roleRepository.findByName("ROLE_USER")
        val newUser = UserEntity().apply {
            this.id = id
            email = anEmail.lowercase()
            password = passwordEncoder.encode(aPassword)
            displayName = aDisplayName
            enabled = true
            if (role.isPresent)
                roles.add(role.get())
        }
        repository.save(newUser)
        return id
    }

    fun checkPassword(userId: UUID, aPassword: String): Boolean {
        val user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        return passwordEncoder.matches(aPassword, user.password)
    }

    fun updatePassword(userId: UUID, aPassword: String) {
        val user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        user.password = passwordEncoder.encode(aPassword)
        repository.save(user)
    }

    fun updateName(userId: UUID, newName: String) {
        val user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        user.displayName = newName
        repository.save(user)
    }

    fun updateRole(userId: UUID) {
        val user = repository.findById(userId).orElseThrow { UserNotFound(userId) }
        val role = roleRepository.findByName("ORGANIZATION_OWNER").orElseThrow { IllegalStateException("Role missing.") }
        user.roles.add(role)
        repository.save(user)
    }

    fun addUserObservatory(observatory: UserController.UserObservatoryRequest, contributor: UserEntity): UserEntity {
        contributor.observatoryId = observatory.observatoryId.value
        contributor.organizationId = observatory.organizationId.value
        return repository.save(contributor)
    }

    fun updateOrganizationAndObservatory(userId: UUID, organizationId: OrganizationId?, observatoryId: ObservatoryId?) {
        val user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        user.organizationId = organizationId?.value
        user.observatoryId = observatoryId?.value
        repository.save(user)
    }

    fun deleteUserObservatory(contributor: UUID) {
        val user = repository.findById(contributor).orElseThrow { UserNotFound(contributor) }
        user.observatoryId = null
        user.organizationId = null
        repository.save(user)
    }
}
