package eu.tib.orkg.prototype.auth.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/**
 * Class representing a user that contributes to the graph.
 *
 * It only contains the subset of information required to represent the contributor in a client.
 */
data class Contributor(
    val id: UUID,
    @JsonProperty("display_name")
    val name: String
)
