package org.orkg.graph.adapter.input.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.LiteralRepresentationAdapter
import org.orkg.graph.domain.LiteralNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UpdateLiteralUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

@RestController
@RequestMapping("/api/literals", produces = [MediaType.APPLICATION_JSON_VALUE])
class LiteralController(
    private val service: LiteralUseCases,
) : LiteralRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
    ): LiteralRepresentation =
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
        pageable: Pageable,
    ): Page<LiteralRepresentation> =
        service.findAll(
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
        ).mapToLiteralRepresentation()

    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody @Valid literal: CreateLiteralRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<LiteralRepresentation> {
        val id = service.create(
            CreateCommand(
                contributorId = currentUser.contributorId(),
                label = literal.label,
                datatype = literal.datatype
            )
        )
        val location = uriComponentsBuilder
            .path("/api/literals/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateLiteralRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<LiteralRepresentation> {
        service.update(
            UpdateLiteralUseCase.UpdateCommand(
                id = id,
                contributorId = currentUser.contributorId(),
                label = request.label,
                datatype = request.datatype
            )
        )
        val location = uriComponentsBuilder
            .path("/api/literals/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateLiteralRequest(
        // No restriction, as we need to support empty values; at least for strings. See TIBHannover/orkg/orkg-backend!152.
        val label: String,
        @field:NotBlank
        val datatype: String = "xsd:string",
    )

    data class UpdateLiteralRequest(
        val label: String?,
        val datatype: String?,
    )
}
