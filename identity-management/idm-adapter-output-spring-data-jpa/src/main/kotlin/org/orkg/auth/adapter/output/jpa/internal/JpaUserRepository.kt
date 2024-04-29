package org.orkg.auth.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>

    override fun findById(id: UUID): Optional<UserEntity>

    fun findAllByObservatoryId(id: UUID, pageable: Pageable): Page<UserEntity>

    fun findByOrganizationId(id: UUID, pageable: Pageable): Page<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.id in ?1")
    fun findByIdIn(@Param("ids")ids: List<UUID>): List<UserEntity>
}
