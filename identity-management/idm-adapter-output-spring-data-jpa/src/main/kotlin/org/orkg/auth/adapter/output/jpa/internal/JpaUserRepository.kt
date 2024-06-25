package org.orkg.auth.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>

    override fun findById(id: UUID): Optional<UserEntity>

    fun findAllByObservatoryId(id: UUID, pageable: Pageable): Page<UserEntity>

    fun findByOrganizationId(id: UUID, pageable: Pageable): Page<UserEntity>
}
