package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateRepresentationAdapter
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
) : BaseController(), TemplateRepresentationAdapter {

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
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("research_problem", required = false) researchProblem: ThingId?,
        @RequestParam("target_class", required = false) targetClass: ThingId?,
        pageable: Pageable
    ): Page<TemplateRepresentation> =
        service.findAll(
            searchString = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            researchField = researchField,
            researchProblem = researchProblem,
            targetClass = targetClass,
            pageable = pageable
        ).mapToTemplateRepresentation()

    @PostMapping(consumes = [TEMPLATE_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @PostMapping("/{id}/properties", consumes = [TEMPLATE_PROPERTY_JSON_V1], produces = [TEMPLATE_PROPERTY_JSON_V1])
    fun createTemplateProperty(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: CreateTemplatePropertyRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = ContributorId(authenticatedUserId())
        service.createTemplateProperty(request.toCreateCommand(userId, id))
        val location = uriComponentsBuilder
            .path("api/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateTemplateRequest(
        @field:NotBlank
        val label: String,
        @field:NotBlank
        val description: String?,
        @field:NotBlank
        @JsonProperty("formatted_label")
        val formattedLabel: String?,
        @JsonProperty("target_class")
        val targetClass: ThingId,
        @field:Valid
        val relations: TemplateRelationsDTO,
        @field:Valid
        @field:Size(min = 1)
        val properties: List<CreateTemplatePropertyRequest>,
        @JsonProperty("is_closed")
        val isClosed: Boolean,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
    ) {
        data class TemplateRelationsDTO(
            @JsonProperty("research_fields")
            val researchFields: List<ThingId>,
            @JsonProperty("research_problems")
            val researchProblems: List<ThingId>,
            val predicate: ThingId?
        ) {
            fun toCreateCommand(): CreateTemplateUseCase.CreateCommand.Relations =
                CreateTemplateUseCase.CreateCommand.Relations(
                    researchFields, researchProblems, predicate
                )
        }

        fun toCreateCommand(contributorId: ContributorId): CreateTemplateUseCase.CreateCommand =
            CreateTemplateUseCase.CreateCommand(
                contributorId = contributorId,
                label = label,
                description = description,
                formattedLabel = formattedLabel?.let { FormattedLabel.of(it) },
                targetClass = targetClass,
                relations = relations.toCreateCommand(),
                properties = properties.map { it.toCreateCommand() },
                isClosed = isClosed,
                observatories = observatories,
                organizations = organizations
            )
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(value = [
        JsonSubTypes.Type(CreateLiteralPropertyRequest::class),
        JsonSubTypes.Type(CreateResourcePropertyRequest::class)
    ])
    sealed interface CreateTemplatePropertyRequest {
        val label: String
        val minCount: Int?
        val maxCount: Int?
        val pattern: String?
        val path: ThingId

        fun toCreateCommand(): TemplatePropertyDefinition

        fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand
    }

    data class CreateLiteralPropertyRequest(
        override val label: String,
        @field:Min(1)
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:Min(1)
        @JsonProperty("max_count")
        override val maxCount: Int?,
        override val pattern: String?,
        override val path: ThingId,
        val datatype: ThingId
    ) : CreateTemplatePropertyRequest {
        override fun toCreateCommand(): TemplatePropertyDefinition =
            CreateTemplateUseCase.CreateCommand.LiteralPropertyDefinition(
                label, minCount, maxCount, pattern, path, datatype
            )

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateLiteralPropertyCommand(
                contributorId, templateId, label, minCount, maxCount, pattern, path, datatype
            )
    }

    data class CreateResourcePropertyRequest(
        override val label: String,
        @field:Min(1)
        @JsonProperty("min_count")
        override val minCount: Int?,
        @field:Min(1)
        @JsonProperty("max_count")
        override val maxCount: Int?,
        override val pattern: String?,
        override val path: ThingId,
        val `class`: ThingId
    ) : CreateTemplatePropertyRequest {
        override fun toCreateCommand(): TemplatePropertyDefinition =
            CreateTemplateUseCase.CreateCommand.ResourcePropertyDefinition(
                label, minCount, maxCount, pattern, path, `class`
            )

        override fun toCreateCommand(
            contributorId: ContributorId,
            templateId: ThingId
        ): CreateTemplatePropertyUseCase.CreateCommand =
            CreateTemplatePropertyUseCase.CreateResourcePropertyCommand(
                contributorId, templateId, label, minCount, maxCount, pattern, path, `class`
            )
    }
}
