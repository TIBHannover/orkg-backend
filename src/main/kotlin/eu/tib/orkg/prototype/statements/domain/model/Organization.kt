package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class Organization(
    val id: OrganizationId?,

    var name: String?,

    var logo: String?,

    @JsonProperty("created_by")
    val createdBy: UUID? = UUID(0, 0),

    var homepage: String?,

    @JsonProperty("observatory_ids")
    val observatoryIds: Set<UUID> = emptySet()
)
