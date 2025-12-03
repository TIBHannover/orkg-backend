package org.orkg.community.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ipfs.multihash.Multihash
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.internal.SHA256
import java.time.OffsetDateTime

/**
 * Class representing a user that contributes to the graph.
 *
 * It only contains the subset of information required to represent the contributor in a client and should be used
 * in all places were user data needs to be displayed. It should never expose sensitive information, such as the
 * user's email address.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Contributor(
    /**
     * The user's ID.
     */
    val id: ContributorId,
    /**
     * The name the users wants to be displayed.
     */
    @get:JsonProperty("display_name")
    val name: String,
    /**
     * The date and time the user joined the project, i.e., the time the corresponding account was created.
     */
    @get:JsonProperty("joined_at")
    val joinedAt: OffsetDateTime,
    /**
     * The ID of the organization the user belongs to.
     */
    @get:JsonProperty("organization_id")
    val organizationId: OrganizationId = OrganizationId.UNKNOWN,
    /**
     * The ID of the observatory the user belongs to.
     */
    @get:JsonProperty("observatory_id")
    val observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
    /**
     * The MD5-hashed email address of the contributor.
     *
     * Used to generate the Gravatar image URL.
     */
    @JsonIgnore
    val emailHash: Multihash,
    /**
     * Determines if the contributor is a curator.
     */
    @JsonIgnore
    val isCurator: Boolean = false,
    /**
     * Determines if the contributor is an admin.
     */
    @JsonIgnore
    val isAdmin: Boolean = false,
) {
    /**
     * The Gravatar ID of the user, e.g., the hashed email address.
     */
    @Suppress("unused")
    @get:JsonProperty("gravatar_id")
    val gravatarId: String = emailHash.toDigestHex()

    /**
     * The URL to an image that represents the user (aka. an avatar).
     *
     * This currently returns a URL to the Gravatar service.
     */
    @Suppress("unused")
    @get:JsonProperty("avatar_url")
    val avatarURL: String = "https://www.gravatar.com/avatar/${emailHash.toDigestHex()}"

    companion object {
        val UNKNOWN: Contributor =
            Contributor(
                id = ContributorId.UNKNOWN,
                name = "Unknown User",
                joinedAt = OffsetDateTime.MIN,
                emailHash = SHA256.ZERO,
            )
    }
}
