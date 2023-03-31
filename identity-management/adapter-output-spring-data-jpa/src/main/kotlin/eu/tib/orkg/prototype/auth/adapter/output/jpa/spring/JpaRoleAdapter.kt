package eu.tib.orkg.prototype.auth.adapter.output.jpa.spring

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaRoleRepository
import eu.tib.orkg.prototype.auth.domain.Role
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.RoleEntity
import eu.tib.orkg.prototype.auth.spi.RoleRepository
import java.util.*
import org.springframework.stereotype.Component

@Component
class JpaRoleAdapter(
    private val repository: JpaRoleRepository,
) : RoleRepository {
    override fun findByName(name: String): Optional<Role> = repository.findByName(name).map(RoleEntity::toRole)
}
