package org.orkg.auth.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.contributorId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyUserController(
    private val contributorRepository: ContributorRepository
) {
    @GetMapping
    fun lookupUserDetails(currentUser: JwtAuthenticationToken?): UserDetails {
        val contributorId = currentUser.contributorId()
        val contributor = contributorRepository.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        return UserDetails.from(contributor, currentUser!!)
    }

    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUserDetails(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.GONE).build()

    @PutMapping("/password", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updatePassword(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.GONE).build()

    /**
     * Decorator for user data.
     * This class prevents user data from leaking by only exposing data that is relevant to the client.
     */
    data class UserDetails(
        @JsonProperty("id")
        val id: ContributorId,
        @JsonProperty("email")
        val email: String,
        @JsonProperty("display_name")
        val displayName: String,
        @JsonProperty("created_at")
        val created: OffsetDateTime,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId?, // nullable for legacy reasons, see orkg-backend#592
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId?, // nullable for legacy reasons, see orkg-backend#592
        @get:JsonProperty("is_curation_allowed")
        val isCurationAllowed: Boolean
    ) {
        companion object {
            fun from(contributor: Contributor, jwtToken: JwtAuthenticationToken): UserDetails =
                UserDetails(
                    id = contributor.id,
                    email = jwtToken.token.getClaim("email"),
                    displayName = contributor.name,
                    created = contributor.joinedAt,
                    organizationId = contributor.organizationId.takeUnless { it == OrganizationId.UNKNOWN },
                    observatoryId = contributor.observatoryId.takeUnless { it == ObservatoryId.UNKNOWN },
                    isCurationAllowed = contributor.isCurator || contributor.isAdmin
                )
        }
    }
}
