package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.contenttypes.adapter.input.rest.mapping.ContentTypeRepresentationAdapter
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.contenttypes.input.ContentTypeUseCases
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

const val RESOURCE_JSON_V1 = "application/vnd.orkg.resource.v1+json"

@RestController
@RequestMapping("/api/content-types", produces = [MediaType.APPLICATION_JSON_VALUE])
class ContentTypeController(
    private val service: ContentTypeUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
) : ContentTypeRepresentationAdapter,
    ResourceRepresentationAdapter {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAll(
        @RequestParam("classes", required = false, defaultValue = "") classes: Set<ContentTypeClass>,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("sdg", required = false) sustainableDevelopmentGoal: ThingId?,
        @RequestParam("author_id", required = false) authorId: ThingId?,
        @RequestParam("author_name", required = false) authorName: String?,
        pageable: Pageable,
    ): Page<ContentTypeRepresentation> {
        if (authorId != null && authorName != null) {
            throw TooManyParameters.atMostOneOf("author_id", "author_name")
        }
        return service.findAll(
            pageable = pageable,
            classes = classes.ifEmpty { ContentTypeClass.entries.toSet() },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            authorId = authorId
        ).mapToContentTypeRepresentation()
    }

    @GetMapping(produces = [RESOURCE_JSON_V1])
    fun findAllAsResource(
        @RequestParam("classes", required = false, defaultValue = "") classes: Set<ContentTypeClass>,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("sdg", required = false) sustainableDevelopmentGoal: ThingId?,
        @RequestParam("author_id", required = false) authorId: ThingId?,
        @RequestParam("author_name", required = false) authorName: String?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> {
        if (authorId != null && authorName != null) {
            throw TooManyParameters.atMostOneOf("author_id", "author_name")
        }
        return service.findAllAsResource(
            pageable = pageable,
            classes = classes.ifEmpty { ContentTypeClass.entries.toSet() },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            authorId = authorId
        ).mapToResourceRepresentation(capabilities)
    }
}
