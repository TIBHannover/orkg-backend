package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateRepresentationAdapter
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.TemplateRelationsDefinition
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase
import org.orkg.contenttypes.input.UpdateTemplateUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
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

const val TEMPLATE_JSON_V1 = "application/vnd.orkg.template.v1+json"
const val TEMPLATE_PROPERTY_JSON_V1 = "application/vnd.orkg.template.property.v1+json"

@RestController
@RequestMapping("/api/templates", produces = [TEMPLATE_JSON_V1])
class TemplateController(
    private val service: TemplateUseCases
) : TemplateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): TemplateRepresentation =
        service.findById(id)
            .mapToTemplateRepresentation()
            .orElseThrow { TemplateNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("research_problem", required = false) researchProblem: ThingId?,
        @RequestParam("target_class", required = false) targetClass: ThingId?,
        pageable: Pageable
    ): Page<TemplateRepresentation> =
        service.findAll(
            label = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            researchProblem = researchProblem,
            targetClass = targetClass,
            pageable = pageable
        ).mapToTemplateRepresentation()

    @RequireLogin
    @PostMapping(consumes = [TEMPLATE_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [TEMPLATE_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping("/{id}/properties", consumes = [TEMPLATE_PROPERTY_JSON_V1], produces = [TEMPLATE_PROPERTY_JSON_V1])
    fun createTemplateProperty(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: TemplatePropertyRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.createTemplateProperty(request.toCreateCommand(userId, id))
        val location = uriComponentsBuilder
            .path("/api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}/properties/{propertyId}", consumes = [TEMPLATE_PROPERTY_JSON_V1], produces = [TEMPLATE_PROPERTY_JSON_V1])
    fun updateTemplateProperty(
        @PathVariable id: ThingId,
        @PathVariable propertyId: ThingId,
        @RequestBody @Valid request: TemplatePropertyRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.updateTemplateProperty(request.toUpdateCommand(propertyId, userId, id))
        val location = uriComponentsBuilder
            .path("/api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateTemplateRequest(
        @field:NotBlank
        val label: String,
        @field:NullableNotBlank
        val description: String?,
        @field:NullableNotBlank
        @JsonProperty("formatted_label")
        val formattedLabel: String?,
        @JsonProperty("target_class")
        val targetClass: ThingId,
        @field:Valid
        val relations: TemplateRelationsDTO,
        @field:Valid
        val properties: List<TemplatePropertyRequest>,
        @JsonProperty("is_closed")
        val isClosed: Boolean,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateTemplateUseCase.CreateCommand =
            CreateTemplateUseCase.CreateCommand(
                contributorId = contributorId,
                label = label,
                description = description,
                formattedLabel = formattedLabel?.let { FormattedLabel.of(it) },
                targetClass = targetClass,
                relations = relations.toTemplateRelations(),
                properties = properties.map { it.toTemplatePropertyDefinition() },
                isClosed = isClosed,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod
            )
    }

    data class UpdateTemplateRequest(
        @field:NotBlank
        val label: String?,
        @field:NullableNotBlank
        val description: String?,
        @field:NullableNotBlank
        @JsonProperty("formatted_label")
        val formattedLabel: String?,
        @JsonProperty("target_class")
        val targetClass: ThingId?,
        @field:Valid
        val relations: TemplateRelationsDTO?,
        @field:Valid
        @field:Size(min = 1)
        val properties: List<TemplatePropertyRequest>?,
        @JsonProperty("is_closed")
        val isClosed: Boolean?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?
    ) {
        fun toUpdateCommand(templateId: ThingId, contributorId: ContributorId): UpdateTemplateUseCase.UpdateCommand =
            UpdateTemplateUseCase.UpdateCommand(
                templateId = templateId,
                contributorId = contributorId,
                label = label,
                description = description,
                formattedLabel = formattedLabel?.let { FormattedLabel.of(it) },
                targetClass = targetClass,
                relations = relations?.toTemplateRelations(),
                properties = properties?.map { it.toTemplatePropertyDefinition() },
                isClosed = isClosed,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                visibility = visibility
            )
    }

    private fun TemplatePropertyRequest.toCreateCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): CreateTemplatePropertyUseCase.CreateCommand =
        when (this) {
            is NumberLiteralPropertyRequest -> toCreateCommand(contributorId, templateId)
            is OtherLiteralPropertyRequest -> toCreateCommand(contributorId, templateId)
            is ResourcePropertyRequest -> toCreateCommand(contributorId, templateId)
            is StringLiteralPropertyRequest -> toCreateCommand(contributorId, templateId)
            is UntypedPropertyRequest -> toCreateCommand(contributorId, templateId)
        }

    private fun TemplatePropertyRequest.toUpdateCommand(
        templatePropertyId: ThingId,
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateTemplatePropertyUseCase.UpdateCommand =
        when (this) {
            is NumberLiteralPropertyRequest -> toUpdateCommand(templatePropertyId, contributorId, templateId)
            is OtherLiteralPropertyRequest -> toUpdateCommand(templatePropertyId, contributorId, templateId)
            is ResourcePropertyRequest -> toUpdateCommand(templatePropertyId, contributorId, templateId)
            is StringLiteralPropertyRequest -> toUpdateCommand(templatePropertyId, contributorId, templateId)
            is UntypedPropertyRequest -> toUpdateCommand(templatePropertyId, contributorId, templateId)
        }

    private fun UntypedPropertyRequest.toCreateCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): CreateTemplatePropertyUseCase.CreateCommand =
        CreateTemplatePropertyUseCase.CreateUntypedPropertyCommand(
            contributorId, templateId, label, placeholder, description, minCount, maxCount, path
        )

    private fun UntypedPropertyRequest.toUpdateCommand(
        templatePropertyId: ThingId,
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateTemplatePropertyUseCase.UpdateCommand =
        UpdateTemplatePropertyUseCase.UpdateUntypedPropertyCommand(
            templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, path
        )

    private fun StringLiteralPropertyRequest.toCreateCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): CreateTemplatePropertyUseCase.CreateCommand =
        CreateTemplatePropertyUseCase.CreateStringLiteralPropertyCommand(
            contributorId, templateId, label, placeholder, description, minCount, maxCount, pattern, path, datatype
        )

    private fun StringLiteralPropertyRequest.toUpdateCommand(
        templatePropertyId: ThingId,
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateTemplatePropertyUseCase.UpdateCommand =
        UpdateTemplatePropertyUseCase.UpdateStringLiteralPropertyCommand(
            templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, pattern, path, datatype
        )

    private fun NumberLiteralPropertyRequest.toCreateCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): CreateTemplatePropertyUseCase.CreateCommand =
        CreateTemplatePropertyUseCase.CreateNumberLiteralPropertyCommand(
            contributorId, templateId, label, placeholder, description, minCount, maxCount, minInclusive, maxInclusive, path, datatype
        )

    private fun NumberLiteralPropertyRequest.toUpdateCommand(
        templatePropertyId: ThingId,
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateTemplatePropertyUseCase.UpdateCommand =
        UpdateTemplatePropertyUseCase.UpdateNumberLiteralPropertyCommand(
            templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, minInclusive, maxInclusive, path, datatype
        )

    private fun OtherLiteralPropertyRequest.toCreateCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): CreateTemplatePropertyUseCase.CreateCommand =
        CreateTemplatePropertyUseCase.CreateOtherLiteralPropertyCommand(
            contributorId, templateId, label, placeholder, description, minCount, maxCount, path, datatype
        )

    private fun OtherLiteralPropertyRequest.toUpdateCommand(
        templatePropertyId: ThingId,
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateTemplatePropertyUseCase.UpdateCommand =
        UpdateTemplatePropertyUseCase.UpdateOtherLiteralPropertyCommand(
            templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, path, datatype
        )

    private fun ResourcePropertyRequest.toCreateCommand(
        contributorId: ContributorId,
        templateId: ThingId
    ): CreateTemplatePropertyUseCase.CreateCommand =
        CreateTemplatePropertyUseCase.CreateResourcePropertyCommand(
            contributorId, templateId, label, placeholder, description, minCount, maxCount, path, `class`
        )

    private fun ResourcePropertyRequest.toUpdateCommand(
        templatePropertyId: ThingId,
        contributorId: ContributorId,
        templateId: ThingId
    ): UpdateTemplatePropertyUseCase.UpdateCommand =
        UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand(
            templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, path, `class`
        )

    data class TemplateRelationsDTO(
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        @JsonProperty("research_problems")
        val researchProblems: List<ThingId>,
        val predicate: ThingId?
    ) {
        fun toTemplateRelations(): TemplateRelationsDefinition =
            TemplateRelationsDefinition(researchFields, researchProblems, predicate)
    }
}
