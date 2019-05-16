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
    fun findById(@PathVariable statementId: StatementId): HttpEntity<StatementResponse> {
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

    @GetMapping("/object/{objectId}")
    fun findByObject(@PathVariable objectId: String): HttpEntity<Iterable<StatementResponse>> =
        ok(
            statementWithResourceService.findAllByObject(ResourceId(objectId)) +
                statementWithLiteralService.findAllByObject(LiteralId(objectId))
        )

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody statement: Statement, uriComponentsBuilder: UriComponentsBuilder):
        HttpEntity<StatementResponse> {
        val body = when (statement.`object`) {
            is Object.Resource -> statementWithResourceService.create(
                statement.subjectId,
                statement.predicateId,
                statement.`object`.id
            )
            is Object.Literal -> statementWithLiteralService.create(
                statement.subjectId,
                statement.predicateId,
                statement.`object`.id
            )
        }

        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.statementId)
            .toUri()

        return created(location).body(body)
    }
}
