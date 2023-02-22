package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.ClassRepresentation
import eu.tib.orkg.prototype.statements.api.LiteralRepresentation
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.CreateStatement
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
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
class StatementController(
    private val statementService: StatementUseCases
) : BaseController() {

    @GetMapping("/")
    fun findAll(
        pageable: Pageable
    ): Iterable<StatementRepresentation> {
        return statementService.findAll(pageable)
    }

    @GetMapping("/{statementId}")
    fun findById(@PathVariable statementId: StatementId): StatementRepresentation =
        statementService.findById(statementId).orElseThrow { StatementNotFound(statementId) }

    @GetMapping("/subject/{subjectId}")
    fun findBySubject(
        @PathVariable subjectId: String,
        pageable: Pageable
    ): HttpEntity<Page<StatementRepresentation>> {
        return ok(statementService.findAllBySubject(subjectId, pageable))
    }

    @GetMapping("/subject/{subjectId}/predicate/{predicateId}")
    fun findBySubjectAndPredicate(
        @PathVariable subjectId: String,
        @PathVariable predicateId: ThingId,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementRepresentation>> {
        return ok(statementService.findAllBySubjectAndPredicate(subjectId, predicateId, pageable))
    }

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(
        @PathVariable predicateId: ThingId,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementRepresentation>> {
        return ok(statementService.findAllByPredicate(predicateId, pageable))
    }

    @GetMapping("/predicate/{predicateId}/literal/{literal}")
    fun findByPredicateAndLiteralAndSubjectClass(
        @PathVariable predicateId: ThingId,
        @PathVariable literal: String,
        @RequestParam("subjectClass", required = false) subjectClass: ThingId?,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementRepresentation>> {
        val result = when (subjectClass) {
            null -> statementService.findAllByPredicateAndLabel(predicateId, literal, pageable)
            else -> statementService.findAllByPredicateAndLabelAndSubjectClass(
                predicateId, literal, subjectClass,
                pageable
            )
        }
        return ok(result)
    }

    @GetMapping("/object/{objectId}")
    fun findByObject(
        @PathVariable objectId: String,
        pageable: Pageable
    ): Page<StatementRepresentation> {
        return statementService.findAllByObject(objectId, pageable)
    }

    @GetMapping("/object/{objectId}/predicate/{predicateId}")
    fun findByObjectAndPredicate(
        @PathVariable objectId: String,
        @PathVariable predicateId: ThingId,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementRepresentation>> {
        return ok(statementService.findAllByObjectAndPredicate(objectId, predicateId, pageable))
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody statement: CreateStatement, uriComponentsBuilder: UriComponentsBuilder):
        HttpEntity<StatementRepresentation> {
        val userId = authenticatedUserId()
        val body = statementService.create(
            ContributorId(userId),
            statement.subjectId,
            statement.predicateId,
            statement.objectId
        )
        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.id)
            .toUri()

        return created(location).body(body)
    }

    @PutMapping("/{id}")
    fun edit(
        @PathVariable id: StatementId,
        @RequestBody(required = true) statementEditRequest: StatementEditRequest
    ): ResponseEntity<StatementRepresentation> {
        val foundStatement = statementService.findById(id)

        if (!foundStatement.isPresent)
            return notFound().build()

        val toUpdate = statementEditRequest
            .copy(
                statementId = id,
                subjectId = statementEditRequest.subjectId ?: getIdAsString(foundStatement.get().subject),
                predicateId = statementEditRequest.predicateId ?: foundStatement.get().predicate.id,
                objectId = statementEditRequest.objectId ?: getIdAsString(foundStatement.get().`object`)
            )

        return ok(statementService.update(toUpdate))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: StatementId
    ): ResponseEntity<Unit> {
        val foundStatement = statementService.findById(id)

        if (!foundStatement.isPresent)
            return notFound().build()

        statementService.remove(foundStatement.get().id)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{thingId}/bundle")
    fun fetchAsBundle(
        @PathVariable thingId: String,
        @RequestParam("minLevel", required = false) minLevel: Int?,
        @RequestParam("maxLevel", required = false) maxLevel: Int?,
        @RequestParam("blacklist", required = false, defaultValue = "") blacklist: List<ThingId>,
        @RequestParam("whitelist", required = false, defaultValue = "") whitelist: List<ThingId>,
        @RequestParam("includeFirst", required = false, defaultValue = "true") includeFirst: Boolean
    ): HttpEntity<Bundle> {
        return ok(
            statementService.fetchAsBundle(
                thingId,
                // FIXME: had to pass configuration like this otherwise lists are not parsed correctly by spring
                BundleConfiguration(
                    minLevel, maxLevel,
                    blacklist, whitelist
                ),
                includeFirst
            )
        )
    }

    private fun getIdAsString(thing: ThingRepresentation): String =
        when (thing) {
            is ResourceRepresentation -> thing.id.value
            is LiteralRepresentation -> thing.id.value
            is PredicateRepresentation -> thing.id.value
            is ClassRepresentation -> thing.id.value
        }
}
