package org.orkg.contenttypes.adapter.input.rest

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.SmartReviewRepresentationAdapter
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val SMART_REVIEW_JSON_V1 = "application/vnd.orkg.smart-review.v1+json"

@RestController
@RequestMapping("/api/smart-reviews", produces = [SMART_REVIEW_JSON_V1])
class SmartReviewController(
    private val service: SmartReviewUseCases
) : SmartReviewRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): SmartReviewRepresentation = service.findById(id)
        .mapToSmartReviewRepresentation()
        .orElseThrow { SmartReviewNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("title", required = false) title: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("published", required = false) published: Boolean?,
        @RequestParam("sdg", required = false) sustainableDevelopmentGoal: ThingId?,
        pageable: Pageable
    ): Page<SmartReviewRepresentation> =
        service.findAll(
            pageable = pageable,
            label = title?.let { SearchString.of(title, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).mapToSmartReviewRepresentation()
}
