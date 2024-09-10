package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeCurator
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.mapping.RosettaStoneStatementRepresentationAdapter
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.input.CreateRosettaStoneStatementUseCase
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.UpdateRosettaStoneStatementUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

const val ROSETTA_STONE_STATEMENT_JSON_V1 = "application/vnd.orkg.rosetta-stone-statement.v1+json"

@RestController
@RequestMapping("/api/rosetta-stone/statements", produces = [ROSETTA_STONE_STATEMENT_JSON_V1])
class RosettaStoneStatementController(
    private val service: RosettaStoneStatementUseCases
) : RosettaStoneStatementRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): RosettaStoneStatementRepresentation =
        service.findByIdOrVersionId(id)
            .mapToRosettaStoneStatementRepresentation(id)
            .orElseThrow { RosettaStoneStatementNotFound(id) }

    @GetMapping("/{id}/versions")
    fun findAllVersionsById(
        @PathVariable id: ThingId
    ): List<RosettaStoneStatementRepresentation> =
        service.findByIdOrVersionId(id)
            .mapToRosettaStoneStatementRepresentation()
            .orElseThrow { RosettaStoneStatementNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("context", required = false) context: ThingId?,
        @RequestParam("template_id", required = false) templateId: ThingId?,
        @RequestParam("class_id", required = false) templateTargetClassId: ThingId?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable
    ): Page<RosettaStoneStatementRepresentation> =
        service.findAll(
            pageable = pageable,
            context = context,
            templateId = templateId,
            templateTargetClassId = templateTargetClassId,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId
        ).mapToRosettaStoneStatementRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [ROSETTA_STONE_STATEMENT_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateRosettaStoneStatementRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/rosetta-stone/statements/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PostMapping("/{id}", consumes = [ROSETTA_STONE_STATEMENT_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateRosettaStoneStatementRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val newId = service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/rosetta-stone/statements/{id}")
            .buildAndExpand(newId)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @DeleteMapping("/{id}")
    fun softDelete(
        @PathVariable id: ThingId,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.softDelete(id, userId)
        return noContent().build()
    }

    @PreAuthorizeCurator
    @DeleteMapping("/{id}/versions")
    fun delete(
        @PathVariable id: ThingId,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.delete(id, userId)
        return noContent().build()
    }

    data class CreateRosettaStoneStatementRequest(
        @JsonProperty("template_id")
        val templateId: ThingId,
        val context: ThingId?,
        @field:Valid
        val subjects: List<String>,
        @field:Valid
        val objects: List<List<String>>,
        val certainty: Certainty,
        val negated: Boolean = false,
        @field:Valid
        val resources: Map<String, ResourceDefinitionDTO>?,
        @field:Valid
        val literals: Map<String, LiteralDefinitionDTO>?,
        @field:Valid
        val predicates: Map<String, PredicateDefinitionDTO>?,
        @field:Valid
        val classes: Map<String, ClassDefinitionDTO>?,
        @field:Valid
        val lists: Map<String, ListDefinitionDTO>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateRosettaStoneStatementUseCase.CreateCommand =
            CreateRosettaStoneStatementUseCase.CreateCommand(
                templateId = templateId,
                contributorId = contributorId,
                context = context,
                subjects = subjects,
                objects = objects,
                certainty = certainty,
                negated = negated,
                extractionMethod = extractionMethod,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                observatories = observatories,
                organizations = organizations
            )
    }

    data class UpdateRosettaStoneStatementRequest(
        @field:Valid
        val subjects: List<String>,
        @field:Valid
        val objects: List<List<String>>,
        val certainty: Certainty,
        val negated: Boolean = false,
        @field:Valid
        val resources: Map<String, ResourceDefinitionDTO>?,
        @field:Valid
        val literals: Map<String, LiteralDefinitionDTO>?,
        @field:Valid
        val predicates: Map<String, PredicateDefinitionDTO>?,
        @field:Valid
        val classes: Map<String, ClassDefinitionDTO>?,
        @field:Valid
        val lists: Map<String, ListDefinitionDTO>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod
    ) {
        fun toUpdateCommand(id: ThingId, contributorId: ContributorId): UpdateRosettaStoneStatementUseCase.UpdateCommand =
            UpdateRosettaStoneStatementUseCase.UpdateCommand(
                id = id,
                contributorId = contributorId,
                subjects = subjects,
                objects = objects,
                certainty = certainty,
                negated = negated,
                extractionMethod = extractionMethod,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                observatories = observatories,
                organizations = organizations
            )
    }
}
