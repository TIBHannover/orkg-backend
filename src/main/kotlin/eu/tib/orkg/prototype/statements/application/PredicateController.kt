package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements/predicates/")
class PredicateController(private val repository: PredicateRepository) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: PredicateId): Predicate =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findAll(): Iterable<PredicateId> = repository.findAll()

    @PostMapping("/")
    fun add(@RequestBody predicate: Predicate): Unit =
        repository.add(predicate)
}
