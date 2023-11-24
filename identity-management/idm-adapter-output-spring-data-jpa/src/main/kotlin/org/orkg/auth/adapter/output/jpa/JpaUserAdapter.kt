package org.orkg.auth.adapter.output.jpa

import java.util.*
import org.orkg.auth.adapter.output.jpa.internal.JpaRoleRepository
import org.orkg.auth.adapter.output.jpa.internal.JpaUserRepository
import org.orkg.auth.adapter.output.jpa.internal.UserEntity
import org.orkg.auth.domain.User
import org.orkg.auth.output.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    override fun deleteAll() = repository.deleteAll()

    override fun count(): Long = repository.count()

    override fun findAll(pageable: Pageable): Page<User> = repository.findAll(pageable).map(UserEntity::toUser)

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
