package eu.tib.orkg.prototype.auth.spi

import eu.tib.orkg.prototype.auth.domain.Role
import java.util.*

interface RoleRepository {
    fun findByName(name: String): Optional<Role>
}
