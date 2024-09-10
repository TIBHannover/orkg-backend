package org.orkg.graph.adapter.input.rest

import java.time.OffsetDateTime
import javax.validation.Valid
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.ClassRepresentationAdapter
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateClassUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/classes", produces = [MediaType.APPLICATION_JSON_VALUE])
class ClassController(
    private val service: ClassUseCases,
    override val statementService: StatementUseCases,
) : ClassRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): ClassRepresentation =
        service.findById(id).mapToClassRepresentation().orElseThrow { ClassNotFound.withThingId(id) }

    @GetMapping(params = ["uri"])
    fun findByURI(@RequestParam uri: ParsedIRI): ClassRepresentation =
        service.findByURI(uri).mapToClassRepresentation().orElseThrow { ClassNotFound.withURI(uri) }

    @GetMapping(params = ["ids"])
    fun findByIds(@RequestParam ids: List<ThingId>, pageable: Pageable): Page<ClassRepresentation> =
        service.findAllById(ids, pageable).mapToClassRepresentation()

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        pageable: Pageable
    ): Page<ClassRepresentation> =
        service.findAll(
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
        ).mapToClassRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody request: CreateClassRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<ClassRepresentation> {
        val id = service.create(
            CreateClassUseCase.CreateCommand(
                contributorId = currentUser.contributorId(),
                id = request.id,
                label = request.label,
                uri = request.uri,
            )
        )
        val location = uriComponentsBuilder
            .path("/api/classes/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).body(service.findById(id).mapToClassRepresentation().get())
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun replace(
        @PathVariable id: ThingId,
        @RequestBody request: ReplaceClassRequest
    ): ClassRepresentation {
        service.replace(UpdateClassUseCase.ReplaceCommand(id, request.label, request.uri))
        return service.findById(id).mapToClassRepresentation().get()
    }

    @PreAuthorizeUser
    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @Valid @RequestBody request: UpdateClassRequest
    ): ResponseEntity<Any> {
        service.update(UpdateClassUseCase.UpdateCommand(id, request.label, request.uri))
        return ok().build()
    }

    data class CreateClassRequest(
        val id: ThingId?,
        val label: String,
        val uri: ParsedIRI?
    )

    data class UpdateClassRequest(
        val label: String? = null,
        val uri: ParsedIRI? = null,
    )

    data class ReplaceClassRequest(
        val label: String,
        val uri: ParsedIRI? = null,
    )
}
