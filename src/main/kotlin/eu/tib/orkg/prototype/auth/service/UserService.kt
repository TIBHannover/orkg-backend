package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.RoleEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Service
@Transactional
class UserService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun findByEmail(email: String): Optional<UserEntity> {
        return repository.findByEmail(email)
    }

    fun findById(id: UUID): Optional<UserEntity> = repository.findById(id)

    fun registerUser(anEmail: String, aPassword: String) {
        val userId = UUID.randomUUID()
        val newUser = UserEntity().apply {
            id = userId
            email = anEmail
            password = passwordEncoder.encode(aPassword)
            enabled = true
            roles = mutableSetOf(RoleEntity().apply {
                name = "ROLE_USER"
                id = userId
            })
        }
        repository.save(newUser)
    }
}
