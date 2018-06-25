package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements/predicates/")
@CrossOrigin(origins = ["*"])
class PredicateController(private val repository: PredicateRepository) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: PredicateId): Predicate =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam(
            "q",
            required = false
        ) searchString: String?
    ) = if (searchString == null)
        repository.findAll()
    else
        repository.findByLabel(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody predicate: Predicate): Predicate {
        val (id, predicateWithId) = if (predicate.id == null) {
            val id = repository.nextIdentity()
            Pair(id, predicate.copy(id = id))
        } else {
            Pair(predicate.id, predicate)
        }
        repository.add(predicateWithId)
        return repository.findById(id).get()
    }
}
