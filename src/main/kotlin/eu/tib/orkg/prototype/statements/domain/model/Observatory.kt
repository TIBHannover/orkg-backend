package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.UUID

data class Observatory(
    val id: UUID?,
    var name: String?,
    var description: String?,
    @JsonProperty("research_field")
    var researchField: String?,
    val users: Set<UserEntity>?,
    val organizations: Set<OrganizationEntity>?,
    var numPapers: Long,
    var numComparisons: Long
)
