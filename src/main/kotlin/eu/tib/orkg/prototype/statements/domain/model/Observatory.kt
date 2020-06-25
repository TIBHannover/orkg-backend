package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.UUID

data class Observatory(
    val id: UUID?,
    val name: String?,
    val description: String?,
    val users: Set<UserEntity>? = mutableSetOf(),
    val organizations: Set<OrganizationEntity>? = mutableSetOf()
)
