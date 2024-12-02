package org.orkg.auth.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository :
    JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>

    override fun findById(id: UUID): Optional<UserEntity>
}
