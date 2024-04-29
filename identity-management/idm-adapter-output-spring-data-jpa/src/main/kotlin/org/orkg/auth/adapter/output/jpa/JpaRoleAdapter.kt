package org.orkg.auth.adapter.output.jpa

import java.util.*
import org.orkg.auth.adapter.output.jpa.internal.JpaRoleRepository
import org.orkg.auth.adapter.output.jpa.internal.RoleEntity
import org.orkg.auth.domain.Role
import org.orkg.auth.output.RoleRepository
import org.springframework.stereotype.Component

@Component
class JpaRoleAdapter(
    private val repository: JpaRoleRepository,
) : RoleRepository {
    override fun findByName(name: String): Optional<Role> = repository.findByName(name).map(RoleEntity::toRole)
}
