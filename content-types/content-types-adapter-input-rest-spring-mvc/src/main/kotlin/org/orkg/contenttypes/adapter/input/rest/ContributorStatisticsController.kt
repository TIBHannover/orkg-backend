package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.ContributorRecordRepresentationAdapter
import org.orkg.contenttypes.input.ContributorStatisticsUseCases
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
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class ContributorStatisticsController(
    private val service: ContributorStatisticsUseCases,
) : ContributorRecordRepresentationAdapter {
    @GetMapping("/api/contributor-statistics")
    fun findAll(
        @RequestParam("after", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) after: OffsetDateTime?,
        @RequestParam("before", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) before: OffsetDateTime?,
        pageable: Pageable,
    ): Page<ContributorRecordRepresentation> =
        service.findAll(
            pageable = pageable,
            after = after,
            before = before,
        ).mapToContributorRecordRepresentation()

    @GetMapping("/api/research-fields/{id}/contributor-statistics")
    fun findAllByResearchFieldId(
        @PathVariable("id") researchField: ThingId,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("after", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) after: OffsetDateTime?,
        @RequestParam("before", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) before: OffsetDateTime?,
        pageable: Pageable,
    ): Page<ContributorRecordRepresentation> =
        service.findAllByResearchFieldId(
            pageable = pageable,
            researchField = researchField,
            includeSubfields = includeSubfields,
            after = after,
            before = before,
        ).mapToContributorRecordRepresentation()

    @GetMapping("/api/research-problems/{id}/contributor-statistics")
    fun findAllByResearchProblemId(
        @PathVariable("id") researchProblem: ThingId,
        @RequestParam("include_subproblems", required = false) includeSubproblems: Boolean = false,
        @RequestParam("after", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) after: OffsetDateTime?,
        @RequestParam("before", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) before: OffsetDateTime?,
        pageable: Pageable,
    ): Page<ContributorRecordRepresentation> =
        service.findAllByResearchProblemId(
            pageable = pageable,
            researchProblem = researchProblem,
            includeSubproblems = includeSubproblems,
            after = after,
            before = before,
        ).mapToContributorRecordRepresentation()
}
