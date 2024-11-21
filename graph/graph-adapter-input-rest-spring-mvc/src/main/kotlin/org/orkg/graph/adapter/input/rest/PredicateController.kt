package org.orkg.graph.adapter.input.rest

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.PredicateRepresentationAdapter
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdatePredicateUseCase.ReplaceCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.Authentication
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
@RequestMapping("/api/predicates", produces = [MediaType.APPLICATION_JSON_VALUE])
class PredicateController(
    override val statementService: StatementUseCases,
    private val service: PredicateUseCases
) : PredicateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): PredicateRepresentation =
        service.findById(id).mapToPredicateRepresentation().orElseThrow { PredicateNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        pageable: Pageable
    ): Page<PredicateRepresentation> =
        service.findAll(
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
        ).mapToPredicateRepresentation()

    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody predicate: CreatePredicateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<PredicateRepresentation> {
        val id = service.create(
            CreatePredicateUseCase.CreateCommand(
                contributorId = currentUser.contributorId(),
                id = predicate.id,
                label = predicate.label,
            )
        )
        val location = uriComponentsBuilder
            .path("/api/predicates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).body(service.findById(id).mapToPredicateRepresentation().get())
    }

    @RequireLogin
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
    @RequireLogin
    fun delete(@PathVariable id: ThingId, currentUser: Authentication?): ResponseEntity<Unit> {
        service.delete(id, currentUser.contributorId())
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
