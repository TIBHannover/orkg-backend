package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val repository: UserRepository,
    private val roleRepository: RoleRepository
) {
    fun findByEmail(email: String): Optional<UserEntity> {
        return repository.findByEmail(email)
    }

    fun findById(id: UUID): Optional<UserEntity> = repository.findById(id)

    fun registerUser(anEmail: String, aPassword: String, aDisplayName: String?) {
        val userId = UUID.randomUUID()
        val role = roleRepository.findByName("ROLE_USER")
        val newUser = UserEntity().apply {
            id = userId
            email = anEmail
            password = "Hello!"
            displayName = aDisplayName
            enabled = true
            if (role.isPresent)
                roles.add(role.get())
        }
        repository.save(newUser)
    }

    fun checkPassword(userId: UUID, aPassword: String): Boolean {
        val user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        // return passwordEncoder.matches(aPassword, user.password)
        return true
    }

    fun updatePassword(userId: UUID, aPassword: String) {
        val user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        // user.password = passwordEncoder.encode(aPassword)
        repository.save(user)
    }

    fun updateName(userId: UUID, newName: String) {
        val user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        user.displayName = newName
        repository.save(user)
    }

    fun updateRole(userId: UUID) {
        val user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        val role = roleRepository.findByName("ORGANIZATION_OWNER").orElseThrow { IllegalStateException("Role missing.") }
        user.roles.add(role)
        repository.save(user)
    }
}
