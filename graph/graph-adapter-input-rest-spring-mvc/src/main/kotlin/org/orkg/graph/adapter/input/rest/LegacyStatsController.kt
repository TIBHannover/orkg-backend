package org.orkg.graph.adapter.input.rest

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ChangeLog
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.domain.ResearchFieldStats
import org.orkg.graph.domain.Stats
import org.orkg.graph.domain.TrendingResearchProblems
import org.orkg.graph.input.LegacyStatisticsUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
class LegacyStatsController(private val service: LegacyStatisticsUseCases) {
    /**
     * Fetch the top statistics of ORKG
     * like paper count, resources count, etc
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun get(
        @RequestParam("extra", required = false) extra: List<String>?,
    ): ResponseEntity<Stats> = ResponseEntity.ok(service.getStats(extra))

    /**
     * Fetch the research fields and
     * their corresponding paper count
     */
    @GetMapping("/fields")
    @ResponseStatus(HttpStatus.OK)
    fun getFields(): ResponseEntity<Map<ThingId, Int>> = ResponseEntity.ok(service.getFieldsStats())

    /**
     * Fetch the papers for each observatory
     */
    @GetMapping("/{id}/stats/papers")
    @ResponseStatus(HttpStatus.OK)
    fun getObservatoryPapersCount(
        @PathVariable id: ObservatoryId,
    ): ResponseEntity<Long> = ResponseEntity.ok(service.getObservatoryPapersCount(id))

    /**
     * Fetch the comparisons by observatory ID
     */
    @GetMapping("/{id}/stats/comparisons")
    @ResponseStatus(HttpStatus.OK)
    fun getObservatoryComparisonsCount(
        @PathVariable id: ObservatoryId,
    ): ResponseEntity<Long> = ResponseEntity.ok(service.getObservatoryComparisonsCount(id))

    /**
     * Fetch the top of research problems
     * in the ORKG System
     */
    @GetMapping("/top/research-problems")
    @ResponseStatus(HttpStatus.OK)
    fun getTrendingResearchProblems(pageable: Pageable): ResponseEntity<Page<TrendingResearchProblems>> =
        ResponseEntity.ok(service.getTrendingResearchProblems(pageable))

    /**
     * Fetch the top changelogs in the
     * ORKG System
     */
    @GetMapping("/top/changelog")
    @ResponseStatus(HttpStatus.OK)
    fun getTopChangeLogs(pageable: Pageable): ResponseEntity<Page<ChangeLog>> =
        ResponseEntity.ok(service.getRecentChangeLog(pageable))

    /**
     * Fetch the top changelogs in the
     * ORKG System by research field ID
     */
    @GetMapping("/research-field/{id}/top/changelog")
    @ResponseStatus(HttpStatus.OK)
    fun getTopChangeLogsByResearchField(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): ResponseEntity<Page<ChangeLog>> =
        ResponseEntity.ok(service.getRecentChangeLogByResearchField(id, pageable))

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

    @GetMapping("/research-fields/{id}")
    fun findResearchFieldStatsById(
        @PathVariable id: ThingId,
        @RequestParam(required = false, defaultValue = "false") includeSubfields: Boolean,
    ): ResearchFieldStats =
        service.findResearchFieldStatsById(id, includeSubfields)
}
