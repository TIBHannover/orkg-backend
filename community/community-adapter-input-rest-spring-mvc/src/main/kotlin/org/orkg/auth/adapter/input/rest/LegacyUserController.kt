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
import org.orkg.community.adapter.output.jpa.internal.toContributor
import org.orkg.community.domain.UserIsAlreadyMemberOfObservatory
import org.orkg.community.domain.UserIsAlreadyMemberOfOrganization
import org.orkg.community.input.ObservatoryAuthUseCases
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    fun updateUserObservatory(@RequestBody @Valid userObservatory: UserObservatoryRequest): ResponseEntity<Any> {
        val user = userService.findByEmail(userObservatory.userEmail).orElseThrow { UserNotFound(userObservatory.userEmail) }
        if (user.observatoryId == userObservatory.observatoryId.value) {
            throw UserIsAlreadyMemberOfObservatory(userObservatory.observatoryId)
        }
        if (user.organizationId == userObservatory.organizationId.value) {
            throw UserIsAlreadyMemberOfOrganization(userObservatory.organizationId)
        }
        return ResponseEntity.ok(
            observatoryAuthUseCases
                .addUserObservatory(userObservatory.observatoryId.value, userObservatory.organizationId.value, user)
                .toContributor()
        )
    }

    @PreAuthorizeCurator
    @DeleteMapping("{id}/observatory")
    fun deleteUserObservatory(@PathVariable id: ContributorId) {
        observatoryAuthUseCases.deleteUserObservatory(id.value)
    }

    data class UserObservatoryRequest(
        @field:Email
        @field:NotBlank
        @JsonProperty("user_email")
        val userEmail: String,
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId
    )
}
