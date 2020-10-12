package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats/")
class StatsController(private val service: StatsService) {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    fun get(): ResponseEntity<Stats> {
        return ResponseEntity.ok(service.getStats())
    }

    @GetMapping("/fields")
    @ResponseStatus(HttpStatus.OK)
    fun getFields(): ResponseEntity<Map<String, Int>> {
        return ResponseEntity.ok(service.getFieldsStats())
    }

    @GetMapping("/{id}/stats/papers")
    @ResponseStatus(HttpStatus.OK)
    fun getObservatoryPapersCount(@PathVariable id: UUID): ResponseEntity<Long> {
        return ResponseEntity.ok(service.getObservatoryPapersCount(id))
    }

    @GetMapping("/{id}/stats/comparisons")
    @ResponseStatus(HttpStatus.OK)
    fun getObservatoryComparisonsCount(@PathVariable id: UUID): ResponseEntity<Long> {
        return ResponseEntity.ok(service.getObservatoryComparisonsCount(id))
    }
}
