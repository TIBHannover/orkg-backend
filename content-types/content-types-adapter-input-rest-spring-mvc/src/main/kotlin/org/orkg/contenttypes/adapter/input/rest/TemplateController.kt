package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateRepresentationAdapter
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.OtherLiteralPropertyDefinition
import org.orkg.contenttypes.input.ResourcePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralPropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplateRelationsDefinition
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.UntypedPropertyDefinition
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase
import org.orkg.contenttypes.input.UpdateTemplateUseCase
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.SearchString
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

    @PreAuthorizeUser
    @PostMapping(consumes = [TEMPLATE_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [TEMPLATE_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @PreAuthorizeUser
    @PostMapping("/{id}/properties", consumes = [TEMPLATE_PROPERTY_JSON_V1], produces = [TEMPLATE_PROPERTY_JSON_V1])
    fun createTemplateProperty(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: TemplatePropertyRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.createTemplateProperty(request.toCreateCommand(userId, id))
        val location = uriComponentsBuilder
            .path("api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{templateId}/properties/{propertyId}", consumes = [TEMPLATE_PROPERTY_JSON_V1], produces = [TEMPLATE_PROPERTY_JSON_V1])
    fun updateTemplateProperty(
        @PathVariable templateId: ThingId,
        @PathVariable propertyId: ThingId,
        @RequestBody @Valid request: TemplatePropertyRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.updateTemplateProperty(request.toUpdateCommand(propertyId, userId, templateId))
        val location = uriComponentsBuilder
            .path("api/templates/{id}")
            .buildAndExpand(templateId)
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
        @field:Size(min = 1)
        val properties: List<TemplatePropertyRequest>,
        @JsonProperty("is_closed")
        val isClosed: Boolean,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
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
                organizations = organizations
            )
    }

    sealed interface TemplatePropertyRequest {
        val label: String
        val placeholder: String?
        val description: String?
        val minCount: Int?
        val maxCount: Int?
        val path: ThingId

        fun toTemplatePropertyDefinition(): TemplatePropertyDefinition

        fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand

        fun toUpdateCommand(
            templatePropertyId: ThingId,
            contributorId: ContributorId,
            templateId: ThingId
        ): UpdateTemplatePropertyUseCase.UpdateCommand
    }

    data class UntypedPropertyRequest(
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        @field:PositiveOrZero
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:PositiveOrZero
        @JsonProperty("max_count")
        override val maxCount: Int?,
        override val path: ThingId
    ) : TemplatePropertyRequest {
        override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
            UntypedPropertyDefinition(label, placeholder, description, minCount, maxCount, path)

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateUntypedPropertyCommand(
                contributorId, templateId, label, placeholder, description, minCount, maxCount, path
            )

        override fun toUpdateCommand(
            templatePropertyId: ThingId,
            contributorId: ContributorId,
            templateId: ThingId
        ): UpdateTemplatePropertyUseCase.UpdateCommand =
            UpdateTemplatePropertyUseCase.UpdateUntypedPropertyCommand(
                templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, path
            )
    }

    data class StringLiteralPropertyRequest(
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        @field:PositiveOrZero
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:PositiveOrZero
        @JsonProperty("max_count")
        override val maxCount: Int?,
        val pattern: String?,
        override val path: ThingId,
        val datatype: ThingId
    ) : TemplatePropertyRequest {
        override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
            StringLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, pattern, path, datatype)

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateStringLiteralPropertyCommand(
                contributorId, templateId, label, placeholder, description, minCount, maxCount, pattern, path, datatype
            )

        override fun toUpdateCommand(
            templatePropertyId: ThingId,
            contributorId: ContributorId,
            templateId: ThingId
        ): UpdateTemplatePropertyUseCase.UpdateCommand =
            UpdateTemplatePropertyUseCase.UpdateStringLiteralPropertyCommand(
                templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, pattern, path, datatype
            )
    }

    data class NumberLiteralPropertyRequest<T : Number>(
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        @field:PositiveOrZero
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:PositiveOrZero
        @JsonProperty("max_count")
        override val maxCount: Int?,
        @JsonProperty("min_inclusive")
        val minInclusive: T?,
        @JsonProperty("max_inclusive")
        val maxInclusive: T?,
        override val path: ThingId,
        val datatype: ThingId
    ) : TemplatePropertyRequest {
        override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
            NumberLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, minInclusive, minInclusive, path, datatype)

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateNumberLiteralPropertyCommand(
                contributorId, templateId, label, placeholder, description, minCount, maxCount, minInclusive, minInclusive, path, datatype
            )

        override fun toUpdateCommand(
            templatePropertyId: ThingId,
            contributorId: ContributorId,
            templateId: ThingId
        ): UpdateTemplatePropertyUseCase.UpdateCommand =
            UpdateTemplatePropertyUseCase.UpdateNumberLiteralPropertyCommand(
                templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, minInclusive, minInclusive, path, datatype
            )
    }

    data class OtherLiteralPropertyRequest(
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        @field:PositiveOrZero
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:PositiveOrZero
        @JsonProperty("max_count")
        override val maxCount: Int?,
        override val path: ThingId,
        val datatype: ThingId
    ) : TemplatePropertyRequest {
        override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
            OtherLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, path, datatype)

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateOtherLiteralPropertyCommand(
                contributorId, templateId, label, placeholder, description, minCount, maxCount, path, datatype
            )

        override fun toUpdateCommand(
            templatePropertyId: ThingId,
            contributorId: ContributorId,
            templateId: ThingId
        ): UpdateTemplatePropertyUseCase.UpdateCommand =
            UpdateTemplatePropertyUseCase.UpdateOtherLiteralPropertyCommand(
                templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, path, datatype
            )
    }

    data class ResourcePropertyRequest(
        override val label: String,
        override val placeholder: String?,
        override val description: String?,
        @field:PositiveOrZero
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:PositiveOrZero
        @JsonProperty("max_count")
        override val maxCount: Int?,
        override val path: ThingId,
        val `class`: ThingId
    ) : TemplatePropertyRequest {
        override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
            ResourcePropertyDefinition(label, placeholder, description, minCount, maxCount, path, `class`)

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateResourcePropertyCommand(
                contributorId, templateId, label, placeholder, description, minCount, maxCount, path, `class`
            )

        override fun toUpdateCommand(
            templatePropertyId: ThingId,
            contributorId: ContributorId,
            templateId: ThingId
        ): UpdateTemplatePropertyUseCase.UpdateCommand =
            UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand(
                templatePropertyId, contributorId, templateId, label, placeholder, description, minCount, maxCount, path, `class`
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
                organizations = organizations
            )
    }

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
