package org.orkg.contenttypes.adapter.input.rest

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateInstanceRepresentationAdapter
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val TEMPLATE_INSTANCE_JSON_V1 = "application/vnd.orkg.template-instance.v1+json"

@RestController
@RequestMapping("/api/templates/{templateId}/instances", produces = [TEMPLATE_INSTANCE_JSON_V1])
class TemplateInstanceController(
    private val service: TemplateInstanceUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService,
) : TemplateInstanceRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable templateId: ThingId,
        @PathVariable id: ThingId
    ): TemplateInstanceRepresentation =
        service.findById(templateId, id)
            .mapToTemplateInstanceRepresentation()
            .orElseThrow { ResourceNotFound.withId(id) }

    @GetMapping
    fun findAll(
        @PathVariable templateId: ThingId,
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable
    ): Page<TemplateInstanceRepresentation> =
        service.findAll(
            templateId = templateId,
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId
        ).mapToTemplateInstanceRepresentation()
}
