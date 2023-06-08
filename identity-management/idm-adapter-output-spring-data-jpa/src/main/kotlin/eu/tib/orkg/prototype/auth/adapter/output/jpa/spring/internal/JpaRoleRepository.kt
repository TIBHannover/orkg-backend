package eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface JpaRoleRepository : JpaRepository<RoleEntity, Int> {
    fun findByName(name: String): Optional<RoleEntity>
}
