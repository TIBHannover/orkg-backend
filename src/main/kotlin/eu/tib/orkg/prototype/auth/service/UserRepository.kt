package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>
    override fun findById(id: UUID): Optional<UserEntity>

    fun findUsersByObservatoryId(id: UUID): Iterable<UserEntity>

    fun findOrganizationById(id: UUID): Optional<UserEntity>

    fun findUsersByOrganizationId(id: UUID): Iterable<UserEntity>
}
