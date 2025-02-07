package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.BundleRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementNotFound
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.ok
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
@RequestMapping("/api/statements", produces = [MediaType.APPLICATION_JSON_VALUE])
class StatementController(
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : StatementRepresentationAdapter, BundleRepresentationAdapter {

    @GetMapping
    fun findAll(
        @RequestParam("subject_classes", required = false) subjectClasses: Set<ThingId>?,
        @RequestParam("subject_id", required = false) subjectId: ThingId?,
        @RequestParam("subject_label", required = false) subjectLabel: String?,
        @RequestParam("predicate_id", required = false) predicateId: ThingId?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("object_classes", required = false) objectClasses: Set<ThingId>?,
        @RequestParam("object_id", required = false) objectId: ThingId?,
        @RequestParam("object_label", required = false) objectLabel: String?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<StatementRepresentation> =
        statementService.findAll(
            pageable = pageable,
            subjectClasses = subjectClasses.orEmpty(),
            subjectId = subjectId,
            subjectLabel = subjectLabel,
            predicateId = predicateId,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            objectClasses = objectClasses.orEmpty(),
            objectId = objectId,
            objectLabel = objectLabel
        ).mapToStatementRepresentation(capabilities)

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: StatementId,
        capabilities: MediaTypeCapabilities
    ): StatementRepresentation =
        statementService.findById(id)
            .mapToStatementRepresentation(capabilities)
            .orElseThrow { StatementNotFound(id) }

    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @RequestBody request: CreateStatementRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
        capabilities: MediaTypeCapabilities
    ): ResponseEntity<StatementRepresentation> {
        val id = statementService.create(
            CreateCommand(
                contributorId = currentUser.contributorId(),
                subjectId = request.subjectId,
                predicateId = request.predicateId,
                objectId = request.objectId
            )
        )
        val location = uriComponentsBuilder
            .path("/api/statements/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location)
            .body(statementService.findById(id).mapToStatementRepresentation(capabilities).get())
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: StatementId,
        @RequestBody request: UpdateStatementRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        capabilities: MediaTypeCapabilities
    ): ResponseEntity<StatementRepresentation> {
        statementService.update(
            UpdateStatementUseCase.UpdateCommand(
                statementId = id,
                subjectId = request.subjectId,
                predicateId = request.predicateId,
                objectId = request.objectId
            )
        )
        val location = uriComponentsBuilder
            .path("/api/statements/{id}")
            .buildAndExpand(id)
            .toUri()
        return ok().location(location).body(statementService.findById(id).mapToStatementRepresentation(capabilities).get())
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: StatementId
    ): ResponseEntity<Unit> {
        statementService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/bundle")
    fun fetchAsBundle(
        @PathVariable id: ThingId,
        @RequestParam("minLevel", required = false) minLevel: Int?,
        @RequestParam("maxLevel", required = false) maxLevel: Int?,
        @RequestParam("blacklist", required = false, defaultValue = "") blacklist: List<ThingId>,
        @RequestParam("whitelist", required = false, defaultValue = "") whitelist: List<ThingId>,
        @RequestParam("includeFirst", required = false, defaultValue = "true") includeFirst: Boolean,
        sort: Sort,
        capabilities: MediaTypeCapabilities
    ): BundleRepresentation =
        statementService.fetchAsBundle(
            id,
            BundleConfiguration(
                minLevel, maxLevel,
                blacklist, whitelist
            ),
            includeFirst,
            sort
        ).toBundleRepresentation(capabilities)

    data class CreateStatementRequest(
        val id: StatementId? = null,
        @JsonProperty("subject_id")
        val subjectId: ThingId,
        @JsonProperty("predicate_id")
        val predicateId: ThingId,
        @JsonProperty("object_id")
        val objectId: ThingId
    )

    data class UpdateStatementRequest(
        @JsonProperty("subject_id")
        val subjectId: ThingId?,
        @JsonProperty("predicate_id")
        val predicateId: ThingId?,
        @JsonProperty("object_id")
        val objectId: ThingId?
    )
}
