package org.orkg.graph.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeCurator
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.PredicateRepresentationAdapter
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.UpdatePredicateUseCase.ReplaceCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/predicates/", produces = [MediaType.APPLICATION_JSON_VALUE])
class PredicateController(
    private val service: PredicateUseCases
) : PredicateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): PredicateRepresentation =
        service.findById(id).mapToPredicateRepresentation().orElseThrow { PredicateNotFound(id) }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<PredicateRepresentation> =
        when (string) {
            null -> service.findAll(pageable)
            else -> service.findAllByLabel(SearchString.of(string, exactMatch), pageable)
        }.mapToPredicateRepresentation()

    @PreAuthorizeUser
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody predicate: CreatePredicateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<PredicateRepresentation> {
        val id = service.create(
            CreatePredicateUseCase.CreateCommand(
                contributorId = currentUser.contributorId(),
                id = predicate.id,
                label = predicate.label,
            )
        )
        val location = uriComponentsBuilder
            .path("api/predicates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).body(service.findById(id).mapToPredicateRepresentation().get())
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody predicate: ReplacePredicateRequest
    ): ResponseEntity<PredicateRepresentation> {
        val found = service.findById(id)

        if (!found.isPresent) return ResponseEntity.notFound().build()

        service.update(id, ReplaceCommand(label = predicate.label, description = predicate.description))

        return ResponseEntity.ok(findById(id))
    }

    @DeleteMapping("/{id}")
    @PreAuthorizeCurator
    fun delete(@PathVariable id: ThingId): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class CreatePredicateRequest(
        val id: ThingId?,
        val label: String
    )

    data class ReplacePredicateRequest(
        val label: String,
        val description: String? = null,
    )
}
