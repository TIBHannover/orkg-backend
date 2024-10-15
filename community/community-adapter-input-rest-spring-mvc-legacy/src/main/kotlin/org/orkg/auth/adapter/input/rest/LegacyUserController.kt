package org.orkg.auth.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import org.orkg.auth.domain.UserNotFound
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.annotations.PreAuthorizeCurator
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.UserIsAlreadyMemberOfObservatory
import org.orkg.community.domain.UserIsAlreadyMemberOfOrganization
import org.orkg.community.input.ObservatoryAuthUseCases
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * This controller provides some deprecated API endpoints to manage observatories for backwards-compatibility.
 * They were wrongly placed under `/api/user` and should not be used anymore.
 */
@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyUserController(
    private val userService: AuthUseCase,
    private val observatoryAuthUseCases: ObservatoryAuthUseCases,
) {
    @PreAuthorizeCurator
    @PutMapping("/observatory", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUserObservatoryLegacy(@RequestBody @Valid request: UserObservatoryRequest): Contributor =
        when (request) {
            is LegacyUserObservatoryRequest -> {
                val user = userService.findByEmail(request.userEmail)
                    .orElseThrow { UserNotFound(request.userEmail) }
                if (user.observatoryId == request.observatoryId.value) {
                    throw UserIsAlreadyMemberOfObservatory(request.observatoryId)
                }
                if (user.organizationId == request.organizationId.value) {
                    throw UserIsAlreadyMemberOfOrganization(request.organizationId)
                }
                observatoryAuthUseCases.addUserObservatory(
                    observatoryId = request.observatoryId,
                    organizationId = request.organizationId,
                    contributorId = ContributorId(user.id)
                )
            }
            is NewUserObservatoryRequest -> {
                observatoryAuthUseCases.addUserObservatory(
                    observatoryId = request.observatoryId,
                    organizationId = request.organizationId,
                    contributorId = request.contributorId
                )
            }
        }

    @PreAuthorizeCurator
    @DeleteMapping("/{id}/observatory")
    fun deleteUserObservatory(@PathVariable id: ContributorId) {
        observatoryAuthUseCases.deleteUserObservatory(id)
    }

    sealed interface UserObservatoryRequest

    @Deprecated("To be removed")
    data class LegacyUserObservatoryRequest(
        @field:Email
        @field:NotBlank
        @JsonProperty("user_email")
        val userEmail: String,
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId
    ) : UserObservatoryRequest

    data class NewUserObservatoryRequest(
        @JsonProperty("contributor_id")
        val contributorId: ContributorId,
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId
    ) : UserObservatoryRequest
}
