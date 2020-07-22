package eu.tib.orkg.prototype.contributions.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/**
 * Class representing a user that contributes to the graph.
 *
 * It only contains the subset of information required to represent the contributor in a client and should be used
 * in all places were user data needs to be displayed. It should never expose sensitive information, such as the
 * user's email address.
 */
data class Contributor(
    val id: UUID,
    @JsonProperty("display_name")
    val name: String
)
