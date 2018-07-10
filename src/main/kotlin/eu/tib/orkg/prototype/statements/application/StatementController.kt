package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Object
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity.created
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/statements")
@CrossOrigin(origins = ["*"])
class StatementController(private val repository: StatementRepository) {

    @GetMapping("/")
    fun findAll(): Iterable<Statement> {
        return repository.findAll()
    }

    @GetMapping("/subject/{resourceId}")
    fun findByResource(@PathVariable resourceId: ResourceId) =
        repository.findBySubject(resourceId)

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(@PathVariable predicateId: PredicateId) =
        repository.findByPredicate(predicateId)

    @PostMapping("/")
    fun add(@RequestBody statement: Statement) =
        repository.add(statement)

    @PostMapping("/{subjectId}/{predicateId}/{objectId}")
    @ResponseStatus(CREATED)
    fun createWithObjectResource(
        @PathVariable subjectId: ResourceId,
        @PathVariable predicateId: PredicateId,
        @PathVariable objectId: ResourceId,
        uriComponentsBuilder: UriComponentsBuilder
    ): HttpEntity<Statement> {
        val statement = Statement(
            statementId = repository.nextIdentity(),
            subject = subjectId,
            predicate = predicateId,
            `object` = Object.Resource(objectId)
        )

        // TODO: should error if parts not found?
        repository.add(statement)

        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.statementId)
            .toUri()

        return created(location).body(statement)
    }

    @PostMapping("/{subjectId}/{predicateId}")
    @ResponseStatus(CREATED)
    fun createWithObjectLiteral(
        @PathVariable subjectId: ResourceId,
        @PathVariable predicateId: PredicateId,
        @RequestBody `object`: Object.Literal,
        uriComponentsBuilder: UriComponentsBuilder
    ): HttpEntity<Statement> {
        val statement = Statement(
            statementId = repository.nextIdentity(),
            subject = subjectId,
            predicate = predicateId,
            `object` = `object`
        )

        // TODO: should error if parts not found?
        repository.add(statement)

        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.statementId)
            .toUri()

        return created(location).body(statement)
    }
}
