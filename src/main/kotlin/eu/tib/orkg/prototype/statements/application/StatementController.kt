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
class StatementController(
    private val statementWithResourceService: StatementWithResourceService,
    private val statementWithLiteralService: StatementWithLiteralService
) {

    @GetMapping("/")
    fun findAll(): Iterable<StatementResponse> {
        return statementWithResourceService.findAll() + statementWithLiteralService.findAll()
    }

    @GetMapping("/{statementId}")
    fun findById(@PathVariable statementId: Long): HttpEntity<StatementResponse> {
        val foundResourceStatement =
            statementWithResourceService.findById(statementId)
        if (foundResourceStatement.isPresent)
            return ok(foundResourceStatement.get())

        val foundLiteralStatement =
            statementWithLiteralService.findById(statementId)
        if (foundLiteralStatement.isPresent)
            return ok(foundLiteralStatement.get())

        return notFound().build()
    }

    @GetMapping("/subject/{resourceId}")
    fun findByResource(@PathVariable resourceId: ResourceId): HttpEntity<Iterable<StatementResponse>> =
        ok(
            statementWithResourceService.findAllBySubject(resourceId) +
                statementWithLiteralService.findAllBySubject(resourceId)
        )

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(@PathVariable predicateId: PredicateId): HttpEntity<Iterable<StatementResponse>> =
        ok(
            statementWithResourceService.findAllByPredicate(predicateId) +
                statementWithLiteralService.findAllByPredicate(predicateId)
        )

    @PostMapping("/")
    // FIXME: how can we deal with that without null issues?
    fun add(@RequestBody statement: StatementWithResource) =
        statementWithResourceService.create(
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
        val statement = statementWithResourceService.create(
            subjectId,
            predicateId,
            objectId
        )

        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.id)
            .toUri()

        return created(location).body(statement)
    }

    @PostMapping("/{subjectId}/{predicateId}")
    @ResponseStatus(CREATED)
    fun createWithLiteralObject(
        @PathVariable subjectId: ResourceId,
        @PathVariable predicateId: PredicateId,
        @RequestBody body: StatementWithLiteralRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): HttpEntity<StatementWithLiteral> {
        // TODO: should error if parts not found?
        val statement =
            statementWithLiteralService.create(
                subjectId,
                predicateId,
                body.`object`.id
            )

        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.id)
            .toUri()

        return created(location).body(statement)
    }
}
