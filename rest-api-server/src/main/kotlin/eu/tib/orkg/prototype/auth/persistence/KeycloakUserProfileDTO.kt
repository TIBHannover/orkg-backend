package eu.tib.orkg.prototype.auth.persistence

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class KeycloakUserProfileDTO(
    @JsonProperty("keycloak_id")
    val keycloakId: UUID? = null,
    @JsonProperty("display_name")
    val displayName: String? = null,
    @JsonProperty("email")
    val email: String? = null,
    @JsonProperty("first_name")
    val firstName: String? = null,
    @JsonProperty("last_name")
    val lastName: String? = null,
    @JsonProperty("name")
    val name: String? = null
)
