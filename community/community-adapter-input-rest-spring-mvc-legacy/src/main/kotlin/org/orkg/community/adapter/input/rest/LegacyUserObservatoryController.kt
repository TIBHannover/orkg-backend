package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.community.domain.Contributor
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
class LegacyUserObservatoryController(
    private val observatoryAuthUseCases: ObservatoryAuthUseCases,
) {
    @RequireCuratorRole
    @PutMapping("/observatory", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUserObservatoryLegacy(@RequestBody @Valid request: UserObservatoryRequest): Contributor =
        observatoryAuthUseCases.addUserObservatory(
            observatoryId = request.observatoryId,
            organizationId = request.organizationId,
            contributorId = request.contributorId
        )

    @RequireCuratorRole
    @DeleteMapping("/{id}/observatory")
    fun deleteUserObservatory(@PathVariable id: ContributorId) {
        observatoryAuthUseCases.deleteUserObservatory(id)
    }

    data class UserObservatoryRequest(
        @JsonProperty("contributor_id")
        val contributorId: ContributorId,
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId
    )
}