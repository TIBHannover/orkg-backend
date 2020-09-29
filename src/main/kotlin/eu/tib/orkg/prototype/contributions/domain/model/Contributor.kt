package eu.tib.orkg.prototype.contributions.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.domain.model.GravatarId
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Class representing a user that contributes to the graph.
 *
 * It only contains the subset of information required to represent the contributor in a client and should be used
 * in all places were user data needs to be displayed. It should never expose sensitive information, such as the
 * user's email address.
 */
data class Contributor(
    /**
     * The user's ID.
     */
    val id: UUID,

    /**
     * The name the users wants to be displayed.
     */
    @JsonProperty("display_name")
    val name: String,

    /**
     * The date and time the user joined the project, i.e. the time the corresponding account was created.
     */
    @JsonProperty("joined_at")
    val joinedAt: OffsetDateTime,

    /**
     * The ID of the organization the user belongs to.
     */
    @JsonProperty("organization_id")
    val organizationId: UUID = UUID(0, 0),

    /**
     * The ID of the observatory the user belongs to.
     */
    @JsonProperty("observatory_id")
    val observatoryId: UUID = UUID(0, 0),

    /**
     * The email address of the contributor.
     *
     * Used to generate the Gravatar image URL.
     */
    @JsonIgnore
    private val email: String? = null
) {
    /**
     * The Gravatar ID of the user, e.g. the hashed email address.
     */
    @get:JsonProperty("gravatar_id")
    val gravatarId: String
        get() = GravatarId(email).imageURL()
}
