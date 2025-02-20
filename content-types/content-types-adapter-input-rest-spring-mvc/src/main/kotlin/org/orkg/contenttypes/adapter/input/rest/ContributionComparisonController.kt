package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionInfo
import org.orkg.contenttypes.domain.TooFewIDsError
import org.orkg.contenttypes.input.ComparisonContributionsUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class ContributionComparisonController(
    private val retrieveContributionComparisons: ComparisonContributionsUseCases,
) {
    @GetMapping("/api/contribution-comparisons/contributions")
    fun findAllContributionDetailsById(
        @RequestParam("ids") contributionIds: List<ThingId>,
        pageable: Pageable,
    ): Page<ContributionInfo> {
        if (contributionIds.size < 2) {
            throw TooFewIDsError(contributionIds)
        }
        return retrieveContributionComparisons.findAllContributionDetailsById(contributionIds, pageable)
    }
}
