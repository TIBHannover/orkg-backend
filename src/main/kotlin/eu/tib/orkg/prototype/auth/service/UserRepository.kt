package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>

    override fun findById(id: UUID): Optional<UserEntity>

    fun findByObservatoryId(id: UUID): Iterable<UserEntity>

    fun findByOrganizationId(id: UUID): Iterable<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.id in ?1")
    fun findByIdIn(@Param("ids")ids: Array<UUID>, pageable: Pageable): Page<UserEntity>
}
