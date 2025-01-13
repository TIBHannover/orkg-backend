package org.orkg.graph.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.ListRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ThingRepresentationAdapter
import org.orkg.graph.domain.ListNotFound
import org.orkg.graph.input.CreateListUseCase.CreateCommand
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateListUseCase.UpdateCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/lists", produces = [MediaType.APPLICATION_JSON_VALUE])
class ListController(
    private val service: ListUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ListRepresentationAdapter, ThingRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): ListRepresentation =
        service.findById(id).mapToListRepresentation().orElseThrow { ListNotFound(id) }

    @GetMapping("/{id}/elements")
    fun findAllElementsById(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ThingRepresentation> =
        service.findAllElementsById(id, pageable)
            .mapToThingRepresentation(capabilities)

    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody request: CreateListRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ListRepresentation> {
        val id = service.create(request.toCreateCommand(currentUser.contributorId()))
        val location = uriComponentsBuilder.path("/api/lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).body(findById(id))
    }

    @RequireLogin
    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody request: UpdateRequest,
        currentUser: Authentication?
    ): ResponseEntity<Any> {
        service.update(request.toUpdateCommand(id, currentUser.contributorId()))
        return noContent().build()
    }

    data class CreateListRequest(
        val label: String,
        val elements: List<ThingId>
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateCommand =
            CreateCommand(contributorId, label, elements)
    }

    data class UpdateRequest(
        val label: String? = null,
        val elements: List<ThingId>? = null
    ) {
        fun toUpdateCommand(id: ThingId, contributorId: ContributorId): UpdateCommand =
            UpdateCommand(id, contributorId, label, elements)
    }
}
