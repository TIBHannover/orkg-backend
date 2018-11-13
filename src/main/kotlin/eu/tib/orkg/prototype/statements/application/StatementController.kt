package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.*

@RestController
@RequestMapping("/api/statements")
@CrossOrigin(origins = ["*"])
class StatementController(private val service: StatementWithResourceService) {

    @GetMapping("/")
    fun findAll(): Iterable<StatementWithResource> {
        return service.findAll()
    }

    @GetMapping("/{statementId}")
    fun findById(@PathVariable statementId: Long) =
        service.findById(statementId)

    @GetMapping("/subject/{resourceId}")
    fun findByResource(@PathVariable resourceId: ResourceId) =
        service.findAllBySubject(resourceId)

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(@PathVariable predicateId: PredicateId) =
        service.findAllByPredicate(predicateId)

    @PostMapping("/")
    // FIXME: how can we deal with that without null issues?
    fun add(@RequestBody statement: StatementWithResource) =
        service.create(
            statement.subject.id!!,
            statement.predicate.id!!,
            statement.`object`.id!!
        )

    @PostMapping("/{subjectId}/{predicateId}/{objectId}")
    @ResponseStatus(CREATED)
    fun createWithObjectResource(
        @PathVariable subjectId: ResourceId,
        @PathVariable predicateId: PredicateId,
        @PathVariable objectId: ResourceId,
        uriComponentsBuilder: UriComponentsBuilder
    ): HttpEntity<StatementWithResource> {
        // TODO: should error if parts not found?
        val statement = service.create(subjectId, predicateId, objectId)

        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.id)
            .toUri()

        return created(location).body(statement)
    }
}
