package org.orkg.graph.adapter.input.rest

import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.LiteralRepresentationAdapter
import org.orkg.graph.domain.LiteralNotFound
import org.orkg.graph.domain.PropertyIsBlank
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
@RequestMapping("/api/literals", produces = [MediaType.APPLICATION_JSON_VALUE])
class LiteralController(
    private val service: LiteralUseCases
) : LiteralRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): LiteralRepresentation =
        service.findById(id)
            .mapToLiteralRepresentation()
            .orElseThrow { LiteralNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        pageable: Pageable
    ): Page<LiteralRepresentation> =
        service.findAll(
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
        ).mapToLiteralRepresentation()

    @PreAuthorizeUser
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody @Valid literal: LiteralCreateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<LiteralRepresentation> {
        val id = service.create(
            CreateCommand(
                contributorId = currentUser.contributorId(),
                label = literal.label,
                datatype = literal.datatype
            )
        )
        val location = uriComponentsBuilder
            .path("api/literals/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).mapToLiteralRepresentation().get())
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: LiteralUpdateRequest
    ): LiteralRepresentation {
        var literal = service.findById(id).orElseThrow { LiteralNotFound(id) }

        if (request.label != null) {
            literal = literal.copy(label = request.label)
        }

        if (request.datatype != null) {
            if (request.datatype.isBlank()) throw PropertyIsBlank("datatype")
            literal = literal.copy(datatype = request.datatype)
        }
        service.update(literal)
        return findById(literal.id)
    }

    data class LiteralCreateRequest(
        // No restriction, as we need to support empty values; at lease for strings. See TIBHannover/orkg/orkg-backend!152.
        val label: String,
        @field:NotBlank
        val datatype: String = "xsd:string"
    )

    data class LiteralUpdateRequest(
        val id: ThingId?,
        val label: String?,
        val datatype: String?
    )
}
