package eu.tib.orkg.prototype.statements.domain.model

import java.util.UUID

data class Observatory(
    val id: UUID?,
    val name: String?,
    val organizationId: UUID?
)
