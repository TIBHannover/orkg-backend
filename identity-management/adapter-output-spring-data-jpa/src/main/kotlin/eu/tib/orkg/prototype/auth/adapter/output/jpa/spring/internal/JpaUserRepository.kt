package eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal

import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>

    override fun findById(id: UUID): Optional<UserEntity>

    fun findByObservatoryId(id: UUID): Iterable<UserEntity>

    fun findByOrganizationId(id: UUID): Iterable<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.id in ?1")
    fun findByIdIn(@Param("ids")ids: Array<UUID>): List<UserEntity>
}
