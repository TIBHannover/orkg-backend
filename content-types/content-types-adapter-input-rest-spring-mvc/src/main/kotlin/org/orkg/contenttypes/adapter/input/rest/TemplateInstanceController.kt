package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateInstanceRepresentationAdapter
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.contenttypes.input.UpdateTemplateInstanceUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

const val TEMPLATE_INSTANCE_JSON_V1 = "application/vnd.orkg.template-instance.v1+json"

@RestController
@RequestMapping("/api/templates/{id}/instances", produces = [TEMPLATE_INSTANCE_JSON_V1])
class TemplateInstanceController(
    private val service: TemplateInstanceUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : TemplateInstanceRepresentationAdapter {
    @GetMapping("/{instanceId}")
    fun findById(
        @PathVariable id: ThingId,
        @PathVariable instanceId: ThingId,
        capabilities: MediaTypeCapabilities,
    ): TemplateInstanceRepresentation =
        service.findById(id, instanceId)
            .mapToTemplateInstanceRepresentation(capabilities)
            .orElseThrow { ResourceNotFound.withId(instanceId) }

    @GetMapping
    fun findAll(
        @PathVariable id: ThingId,
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<TemplateInstanceRepresentation> =
        service.findAll(
            templateId = id,
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId
        ).mapToTemplateInstanceRepresentation(capabilities)

    @RequireLogin
    @PutMapping("/{instanceId}", consumes = [TEMPLATE_INSTANCE_JSON_V1], produces = [TEMPLATE_INSTANCE_JSON_V1])
    fun updateTemplateInstance(
        @PathVariable id: ThingId,
        @PathVariable instanceId: ThingId,
        @RequestBody @Valid request: UpdateTemplateInstanceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(userId, id, instanceId))
        val location = uriComponentsBuilder
            .path("/api/templates/{id}/instances/{instanceId}")
            .buildAndExpand(id, instanceId)
            .toUri()
        return noContent().location(location).build()
    }

    data class UpdateTemplateInstanceRequest(
        @field:Valid
        val statements: Map<ThingId, List<String>>,
        @field:Valid
        val resources: Map<String, CreateResourceRequestPart>?,
        @field:Valid
        val literals: Map<String, String>?,
        @field:Valid
        val predicates: Map<String, CreatePredicateRequestPart>?,
        @field:Valid
        val classes: Map<String, CreateClassRequestPart>?,
        @field:Valid
        val lists: Map<String, CreateListRequestPart>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        fun toUpdateCommand(
            contributorId: ContributorId,
            templateId: ThingId,
            id: ThingId,
        ): UpdateTemplateInstanceUseCase.UpdateCommand =
            UpdateTemplateInstanceUseCase.UpdateCommand(
                subject = id,
                templateId = templateId,
                contributorId = contributorId,
                statements = statements,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { CreateLiteralCommandPart(it.value) }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                extractionMethod = extractionMethod
            )
    }
}
