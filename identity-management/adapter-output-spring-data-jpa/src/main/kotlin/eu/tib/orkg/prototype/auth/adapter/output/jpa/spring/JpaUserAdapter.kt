package eu.tib.orkg.prototype.auth.adapter.output.jpa.spring

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaRoleRepository
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.UserEntity
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.auth.spi.UserRepository
import java.util.*
import org.springframework.stereotype.Component

@Component
class JpaUserAdapter(
    private val repository: JpaUserRepository,
    private val roleRepository: JpaRoleRepository,
) : UserRepository {
    override fun save(user: User) {
        repository.save(user.toUserEntity())
    }

    override fun findByEmailIgnoreCase(email: String): Optional<User> =
        repository.findByEmailIgnoreCase(email).map(UserEntity::toUser)

    override fun findById(id: UUID): Optional<User> = repository.findById(id).map(UserEntity::toUser)

    override fun findByObservatoryId(id: UUID): Iterable<User> =
        repository.findByObservatoryId(id).map(UserEntity::toUser)

    override fun findByOrganizationId(id: UUID): Iterable<User> =
        repository.findByOrganizationId(id).map(UserEntity::toUser)

    override fun findByIdIn(ids: Array<UUID>): List<User> = repository.findByIdIn(ids).map(UserEntity::toUser)

    override fun deleteAll() = repository.deleteAll()

    internal fun User.toUserEntity(): UserEntity = repository.findById(this.id).orElse(UserEntity()).apply {
        id = this@toUserEntity.id
        email = this@toUserEntity.email
        password = this@toUserEntity.password
        displayName = this@toUserEntity.displayName
        enabled = this@toUserEntity.enabled
        created = this@toUserEntity.createdAt
        organizationId = this@toUserEntity.organizationId
        observatoryId = this@toUserEntity.observatoryId
        roles = this@toUserEntity.roles
            .map { roleRepository.findByName(it.name).get() }
            .toMutableSet()
    }
}
