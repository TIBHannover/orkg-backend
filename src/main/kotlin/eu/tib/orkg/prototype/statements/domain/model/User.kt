package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity

data class User(
    // val id: UUID?,
    // @JsonProperty("display_name")
    // val displayName: String?
    val users: Set<UserEntity>?
)
