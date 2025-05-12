package org.orkg.graph.adapter.input.rest

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.input.LegacyStatisticsUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Class that provides the statistics
 * of ORKG
 */
@RestController
@RequestMapping("/api/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyStatsController(
    private val service: LegacyStatisticsUseCases,
) {
    /**
     * Fetch the top contributors
     */
    @GetMapping("/top/contributors")
    @ResponseStatus(HttpStatus.OK)
    fun getTopContributors(
        @RequestParam(required = false, defaultValue = "0") days: Long,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        service.getTopCurrentContributors(days, pageable)

    /**
     * Fetch the top contributors by research field ID including subfields
     */
    @GetMapping("/research-field/{id}/subfields/top/contributors")
    @ResponseStatus(HttpStatus.OK)
    fun getTopContributorsByResearchField(
        @PathVariable id: ThingId,
        @RequestParam(required = false, defaultValue = "0") days: Long,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        service.getTopCurrentContributorsByResearchField(id, days, pageable)

    /**
     * Fetch the top contributors by research field ID excluding subfields
     */
    @GetMapping("/research-field/{id}/top/contributors")
    @ResponseStatus(HttpStatus.OK)
    fun getTopContributorsByResearchFieldExcludeSubFields(
        @PathVariable id: ThingId,
        @RequestParam(required = false, defaultValue = "0") days: Long,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        service.getTopCurrentContributorsByResearchFieldExcludeSubFields(id, days, pageable)

    @GetMapping("/observatories")
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        service.findAllObservatoryStats(pageable)

    @GetMapping("/observatories/{id}")
    fun findObservatoryStatsById(
        @PathVariable id: ObservatoryId,
    ): ObservatoryStats =
        service.findObservatoryStatsById(id)
}
