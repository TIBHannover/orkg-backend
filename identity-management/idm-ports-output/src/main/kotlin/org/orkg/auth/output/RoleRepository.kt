package org.orkg.auth.output

import java.util.*
import org.orkg.auth.domain.Role

interface RoleRepository {
    fun findByName(name: String): Optional<Role>
}
