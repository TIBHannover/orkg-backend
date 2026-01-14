package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.AuthorRecordRepresentationAdapter
import org.orkg.contenttypes.input.AuthorStatisticsUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
class AuthorStatisticsController(
    private val service: AuthorStatisticsUseCases,
) : AuthorRecordRepresentationAdapter {
    @GetMapping("/api/research-problems/{id}/author-statistics")
    fun findAllByResearchProblemId(
        @PathVariable id: ThingId,
        @RequestParam("after", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) after: OffsetDateTime?,
        @RequestParam("before", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) before: OffsetDateTime?,
        pageable: Pageable,
    ): Page<AuthorRecordRepresentation> =
        service.findAllByResearchProblemId(
            pageable = pageable,
            researchProblem = id,
            after = after,
            before = before,
        ).mapToAuthorRecordRepresentation()
}
