package org.orkg.graph.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.CreateObjectUseCase
import org.orkg.graph.input.CreateObjectUseCase.CreateObjectRequest
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/objects/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObjectController(
    private val resourceService: ResourceUseCases,
    private val objectService: CreateObjectUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService,
) : ResourceRepresentationAdapter {

    @PreAuthorizeUser
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails,
    ): ResponseEntity<ResourceRepresentation> {
        val id = objectService.createObject(obj, null, currentUser.contributorId().value)
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(resourceService.findById(id).mapToResourceRepresentation().get())
    }

    @PreAuthorizeUser
    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @PathVariable id: ThingId,
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails,
    ): ResponseEntity<ResourceRepresentation> {
        resourceService
            .findById(id)
            .orElseThrow { ResourceNotFound.withId(id) }
        objectService.createObject(obj, id, currentUser.contributorId().value)
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(resourceService.findById(id).mapToResourceRepresentation().get())
    }
}
