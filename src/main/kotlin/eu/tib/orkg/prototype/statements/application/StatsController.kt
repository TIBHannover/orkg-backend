package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.TrendingResearchProblems
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.ChangeLog
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.TopContributorsWithProfile
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.TopContributorsWithProfileAndTotalCount
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
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
@RequestMapping("/api/stats/")
class StatsController(private val service: StatsService) {

    /**
     * Fetch the top statistics of ORKG
     * like paper count, resources count, etc
     */
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    fun get(): ResponseEntity<Stats> {
        return ResponseEntity.ok(service.getStats())
    }

    /**
     * Fetch the research fields and
     * their corresponding paper count
     */
    @GetMapping("/fields")
    @ResponseStatus(HttpStatus.OK)
    fun getFields(): ResponseEntity<Map<String, Int>> {
        return ResponseEntity.ok(service.getFieldsStats())
    }

    /**
     * Fetch the papers for each observatory
     */
    @GetMapping("/{id}/stats/papers")
    @ResponseStatus(HttpStatus.OK)
    fun getObservatoryPapersCount(@PathVariable id: ObservatoryId): ResponseEntity<Long> {
        return ResponseEntity.ok(service.getObservatoryPapersCount(id))
    }

    /**
     * Fetch the comparisons by observatory ID
     */
    @GetMapping("/{id}/stats/comparisons")
    @ResponseStatus(HttpStatus.OK)
    fun getObservatoryComparisonsCount(@PathVariable id: ObservatoryId): ResponseEntity<Long> {
        return ResponseEntity.ok(service.getObservatoryComparisonsCount(id))
    }

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
    fun getTopChangeLogsByResearchField(@PathVariable id: ResourceId, pageable: Pageable): ResponseEntity<Page<ChangeLog>> =
        ResponseEntity.ok(service.getRecentChangeLogByResearchField(id, pageable))

    /**
     * Fetch the top contributors along with
     * the profile details
     */
    @GetMapping("/top/contributors")
    @ResponseStatus(HttpStatus.OK)
    fun getTopContributors(pageable: Pageable): ResponseEntity<Page<TopContributorsWithProfile>> =
        ResponseEntity.ok(service.getTopCurrentContributors(pageable))

    /**
     * Fetch the top contributors by research field ID along with
     * the profile details
     */
    @GetMapping("/research-field/{id}/top/contributors")
    @ResponseStatus(HttpStatus.OK)
    fun getTopContributorsByResearchField(@PathVariable id: ResourceId, @RequestParam days: Optional<Long>): ResponseEntity<Iterable<TopContributorsWithProfileAndTotalCount>> {
        if (days.isPresent) {
            return ResponseEntity.ok(service.getTopCurrentContributorsByResearchField(id, days.get()))
        }
        return ResponseEntity.ok(service.getTopCurrentContributorsByResearchField(id, 0))
    }
}
