package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats/")
@CrossOrigin(origins = ["*"])
class StatsController(private val service: StatsService) {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    fun get(): ResponseEntity<Stats> {
        return ResponseEntity.ok(service.getStats())
    }
}
