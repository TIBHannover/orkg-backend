package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest
import org.orkg.contenttypes.adapter.input.rest.mapping.ContributionRepresentationAdapter
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

const val CONTRIBUTION_JSON_V2 = "application/vnd.orkg.contribution.v2+json"

@RestController
@RequestMapping(produces = [CONTRIBUTION_JSON_V2])
class ContributionController(
    private val service: ContributionUseCases,
) : ContributionRepresentationAdapter {
    @GetMapping("/api/contributions/{id}")
    fun findById(
        @PathVariable id: ThingId,
    ): ContributionRepresentation =
        service.findById(id)
            .mapToContributionRepresentation()
            .orElseThrow { ContributionNotFound(id) }

    @GetMapping("/api/contributions")
    fun findAll(pageable: Pageable): Page<ContributionRepresentation> =
        service.findAll(pageable).mapToContributionRepresentation()

    @RequireLogin
    @PostMapping("/api/papers/{id}/contributions", consumes = [CONTRIBUTION_JSON_V2])
    fun create(
        @PathVariable("id") paperId: ThingId,
        @RequestBody @Valid request: CreateContributionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ContributionRepresentation> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId, paperId))
        val location = uriComponentsBuilder
            .path("/api/contributions/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    data class CreateContributionRequest(
        @field:Valid
        val resources: Map<String, CreateResourceRequestPart>?,
        @field:Valid
        val literals: Map<String, CreateLiteralRequestPart>?,
        @field:Valid
        val predicates: Map<String, CreatePredicateRequestPart>?,
        @field:Valid
        val lists: Map<String, CreateListRequestPart>?,
        @field:Valid
        val contribution: CreatePaperRequest.ContributionRequestPart,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        fun toCreateCommand(contributorId: ContributorId, paperId: ThingId): CreateContributionUseCase.CreateCommand =
            CreateContributionUseCase.CreateCommand(
                contributorId = contributorId,
                paperId = paperId,
                extractionMethod = extractionMethod,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                contribution = contribution.toCreateCommand()
            )
    }
}
