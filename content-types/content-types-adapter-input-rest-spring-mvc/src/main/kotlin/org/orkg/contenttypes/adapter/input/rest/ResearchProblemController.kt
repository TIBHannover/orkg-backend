package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.input.ResearchProblemUseCases
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("/api/research-problems", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResearchProblemController(
    private val service: ResearchProblemUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
        mediaTypeCapabilities: MediaTypeCapabilities,
    ): ResourceRepresentation =
        service.findById(id)
            .mapToResourceRepresentation(mediaTypeCapabilities)
            .orElseThrow { ResearchFieldNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("addressed_by_observatory", required = false) addressedByObservatory: ObservatoryId?,
        @RequestParam("addressed_by_organization", required = false) addressedByOrganization: OrganizationId?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> =
        service.findAll(
            pageable = pageable,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            addressedByObservatory = addressedByObservatory,
            addressedByOrganization = addressedByOrganization,
        ).mapToResourceRepresentation(capabilities)
}
