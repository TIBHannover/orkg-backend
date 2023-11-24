package org.orkg.auth.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface JpaRoleRepository : JpaRepository<RoleEntity, Int> {
    fun findByName(name: String): Optional<RoleEntity>
}
