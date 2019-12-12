package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.Object
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementWithLiteralService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/statements")
@CrossOrigin(origins = ["*"])
class StatementController(
    private val statementWithResourceService: StatementWithResourceService,
    private val statementWithLiteralService: StatementWithLiteralService
) : BaseController() {

    @GetMapping("/")
    fun findAll(
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): Iterable<StatementResponse> {
        // TODO: Check if division by two is the most suitable way or specify the semantics of these endpoints as items per resource/literal
        val pagination = createPageable(page, items, sortBy, desc)
        return (statementWithResourceService.findAll(pagination) + statementWithLiteralService.findAll(pagination)).take(pagination.pageSize)
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
    fun findByResource(
        @PathVariable resourceId: ResourceId,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): HttpEntity<Iterable<StatementResponse>> {
        // TODO: need better selection strategy maybe collect the result into a set and then sort it again based on user criteria and return the requested number of items
        val pagination = createPageable(page, items, sortBy, desc)
        return ok(
            (statementWithResourceService.findAllBySubject(resourceId, pagination) +
                statementWithLiteralService.findAllBySubject(resourceId, pagination)).take(pagination.pageSize)
        )
    }

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(
        @PathVariable predicateId: PredicateId,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): HttpEntity<Iterable<StatementResponse>> {
        val pagination = createPageable(page, items, sortBy, desc)
        return ok(
            (statementWithResourceService.findAllByPredicate(predicateId, pagination) +
                statementWithLiteralService.findAllByPredicate(predicateId, pagination)).take(pagination.pageSize)
        )
    }

    @GetMapping("/object/{objectId}")
    fun findByObject(
        @PathVariable objectId: String,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): HttpEntity<Iterable<StatementResponse>> {
        val pagination = createPageable(page, items, sortBy, desc)
        return ok(
            (statementWithResourceService.findAllByObject(ResourceId(objectId), pagination) +
                statementWithLiteralService.findAllByObject(LiteralId(objectId), pagination)).take(pagination.pageSize)
        )
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody statement: Statement, uriComponentsBuilder: UriComponentsBuilder):
        HttpEntity<StatementResponse> {
        val userId = authenticatedUserId()
        val body = when (statement.`object`) {
            is Object.Resource -> statementWithResourceService.create(
                userId,
                statement.subjectId,
                statement.predicateId,
                statement.`object`.id
            )
            is Object.Literal -> statementWithLiteralService.create(
                userId,
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

    @PutMapping("/{id}")
    fun edit(
        @PathVariable id: StatementId,
        @RequestBody(required = true) statementEditRequest: StatementEditRequest
    ): ResponseEntity<StatementResponse> {
        val foundResourceStatement = statementWithResourceService.findById(id)
        val foundLiteralStatement = statementWithLiteralService.findById(id)

        if (!foundResourceStatement.isPresent && !foundLiteralStatement.isPresent)
            return notFound().build()

        val toUpdate = statementEditRequest
            .copy(
                statementId = id,
                subjectId = statementEditRequest.subjectId ?: if (foundResourceStatement.isPresent) foundResourceStatement.get().subject.id else foundLiteralStatement.get().subject.id,
                predicateId = statementEditRequest.predicateId ?: if (foundResourceStatement.isPresent) foundResourceStatement.get().predicate.id else foundLiteralStatement.get().predicate.id,
                objectId = statementEditRequest.objectId ?: if (foundResourceStatement.isPresent) foundResourceStatement.get().`object`.id else null
            )

        return when (foundResourceStatement.isPresent) {
            true -> ok(statementWithResourceService.update(toUpdate))
            false -> ok(statementWithLiteralService.update(toUpdate))
        }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: StatementId
    ): ResponseEntity<Unit> {
        val foundResourceStatement = statementWithResourceService.findById(id)
        val foundLiteralStatement = statementWithLiteralService.findById(id)

        if (!foundResourceStatement.isPresent && !foundLiteralStatement.isPresent)
            return notFound().build()

        if (foundResourceStatement.isPresent)
            statementWithResourceService.remove(foundResourceStatement.get().id)
        else
            statementWithLiteralService.remove(foundLiteralStatement.get().id)

        return ResponseEntity.noContent().build()
    }
}
