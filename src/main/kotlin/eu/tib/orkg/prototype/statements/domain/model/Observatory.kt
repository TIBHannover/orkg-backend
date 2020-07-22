package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.UUID

data class Observatory(
    val id: UUID?,
    val name: String?,
    val description: String?,
    val users: Set<Contributor> = emptySet(),
    @Deprecated("""The set of organizations is cyclic and will be removed. Use "organization_ids" instead.""")
    val organizations: Set<OrganizationEntity>?,
    @JsonProperty("organization_ids")
    val organizationIds: Set<UUID> = emptySet()
)
