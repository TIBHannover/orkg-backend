package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.RoleEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<RoleEntity, Int> {
    fun findByName(name: String): Optional<RoleEntity>
}
