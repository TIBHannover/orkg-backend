package org.orkg.community.adapter.input.rest

import jakarta.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.community.adapter.input.rest.mapping.ContributorIdentifierRepresentationAdapter
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.input.ContributorIdentifierUseCases
import org.orkg.community.input.CreateContributorIdentifierUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/contributors/{id}/identifiers", produces = [MediaType.APPLICATION_JSON_VALUE])
class ContributorIdentifierController(
    private val contributorIdentifierUseCases: ContributorIdentifierUseCases,
) : ContributorIdentifierRepresentationAdapter {
    @GetMapping
    fun findAllByContributorId(
        @PathVariable id: ContributorId,
        pageable: Pageable,
    ): Page<ContributorIdentifierRepresentation> =
        contributorIdentifierUseCases.findAllByContributorId(id, pageable)
            .mapToContributorIdentifierRepresentation()

    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @PathVariable id: ContributorId,
        @RequestBody @Valid request: CreateContributorIdentifierRequest,
        currentUser: Authentication,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        contributorIdentifierUseCases.create(request.toCreateCommand(contributorId))
        val location = uriComponentsBuilder
            .path("/api/contributors/{id}/identifiers")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @DeleteMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteByContributorIdAndValue(
        @PathVariable id: ContributorId,
        @RequestParam value: String,
        currentUser: Authentication,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        contributorIdentifierUseCases.deleteByContributorIdAndValue(contributorId, value)
        return noContent().build()
    }

    data class CreateContributorIdentifierRequest(
        val type: ContributorIdentifier.Type,
        val value: String,
    ) {
        fun toCreateCommand(contributorId: ContributorId) =
            CreateContributorIdentifierUseCase.CreateCommand(contributorId, type, value)
    }
}
