package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.UnpublishableThing
import org.orkg.contenttypes.domain.configuration.DataCiteConfiguration
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.output.ResourceRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

val publishableClasses: Set<ThingId> = setOf(
    Classes.paper,
    Classes.paperVersion,
    Classes.comparison,
    Classes.smartReviewPublished
)

@RestController
@RequestMapping("/api/dois", produces = [MediaType.APPLICATION_JSON_VALUE])
@Deprecated("To be removed")
class DOIController(
    private val doiService: DoiService,
    private val dataciteConfiguration: DataCiteConfiguration,
    private val resourceRepository: ResourceRepository,
    private val literalService: LiteralUseCases
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun addDOI(@RequestBody @Valid request: CreateDOIRequest): DoiResponse {
        val resource = resourceRepository.findById(request.resourceId)
            .orElseThrow { ResourceNotFound.withId(request.resourceId) }
        if (resource.classes.intersect(publishableClasses).isEmpty()) {
            throw UnpublishableThing(request.resourceId)
        }
        val doi = "${dataciteConfiguration.doiPrefix}/${request.resourceId}"
        doiService.register(
            DoiService.RegisterCommand(
                suffix = request.resourceId.value,
                title = request.title,
                description = request.description,
                subject = request.subject,
                url = request.url,
                creators = request.authors.map { creator ->
                    Author(
                        name = creator.name,
                        identifiers = creator.orcid?.let { mapOf("orcid" to listOf(it)) }
                    )
                },
                resourceType = request.type,
                resourceTypeGeneral = request.resourceType,
                relatedIdentifiers = request.relatedResources.mapNotNull { resourceId ->
                    literalService.findDOIByContributionId(resourceId)
                        .map { it.label }
                        .orElse(null)
                }.distinct()
            )
        )
        return DoiResponse(doi)
    }

    data class DoiResponse(
        val doi: String
    )

    data class CreateDOIRequest(
        @JsonProperty("resource_id")
        val resourceId: ThingId,
        val title: String,
        val subject: String,
        @JsonProperty("related_resources")
        val relatedResources: Set<ThingId>,
        val description: String,
        @field:Valid
        val authors: List<Creator>,
        val url: URI,
        val type: String,
        @JsonProperty("resource_type")
        val resourceType: String
    ) {
        data class Creator(
            @JsonProperty("creator")
            val name: String,
            @field:Size(min = 19, max = 19)
            val orcid: String?
        )
    }
}
