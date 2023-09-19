package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteConfiguration
import eu.tib.orkg.prototype.contenttypes.application.DoiAlreadyRegistered
import eu.tib.orkg.prototype.contenttypes.application.UnpublishableThing
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.LiteralService
import eu.tib.orkg.prototype.statements.services.StatementService
import eu.tib.orkg.prototype.statements.services.publishableClasses
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.net.URI
import javax.validation.constraints.Size
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dois/", produces = [MediaType.APPLICATION_JSON_VALUE])
class DOIController(
    private val doiService: DoiService,
    private val dataciteConfiguration: DataCiteConfiguration,
    private val resourceRepository: ResourceRepository,
    private val statementService: StatementService,
    private val literalService: LiteralService
) {
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun addDOI(@RequestBody request: CreateDOIRequest): DoiResponse {
        val resource = resourceRepository.findById(request.resourceId)
            .orElseThrow { ResourceNotFound.withId(request.resourceId) }
        if (resource.classes.intersect(publishableClasses).isEmpty()) {
            throw UnpublishableThing(request.resourceId)
        }
        val hasDoiStatements = statementService.findAllBySubjectAndPredicate(request.resourceId, Predicates.hasDOI, PageRequests.SINGLE)
        if (!hasDoiStatements.isEmpty) {
            throw DoiAlreadyRegistered(request.resourceId)
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
                        identifiers = creator.orcid?.let { mapOf("orcid" to it) }
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
        val authors: List<Creator>,
        val url: URI,
        val type: String,
        @JsonProperty("resource_type")
        val resourceType: String
    ) {
        data class Creator(
            @JsonProperty("creator")
            val name: String,
            val orcid: String?
        )
    }
}
