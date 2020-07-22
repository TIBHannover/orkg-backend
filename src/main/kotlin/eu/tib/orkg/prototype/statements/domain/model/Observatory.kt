package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.UUID

data class Observatory(
    val id: UUID?,
    val name: String?,
    val description: String?,
    val users: Set<UserEntity>?,
    @Deprecated("""The set of organizations is cyclic and will be removed. Use "organization_ids" instead.""")
    val organizations: Set<OrganizationEntity>?,
    val organization_ids: Set<UUID> = emptySet()
)
