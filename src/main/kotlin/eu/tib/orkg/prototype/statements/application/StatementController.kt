package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.OrkgUserRepository
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.CreateStatement
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.domain.model.Thing
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
import java.security.Principal

@RestController
@RequestMapping("/api/statements")
class StatementController(
    private val statementService: StatementService,
    private val orkgUserRepository: OrkgUserRepository
) : BaseController(orkgUserRepository) {

    @GetMapping("/")
    fun findAll(
        pageable: Pageable
    ): Iterable<StatementResponse> {
        return statementService.findAll(pageable)
    }

    @GetMapping("/{statementId}")
    fun findById(@PathVariable statementId: StatementId): HttpEntity<StatementResponse> {
        val foundStatement =
            statementService.findById(statementId)
        if (foundStatement.isPresent)
            return ok(foundStatement.get())
        return notFound().build()
    }

    @GetMapping("/subject/{subjectId}")
    fun findBySubject(
        @PathVariable subjectId: String,
        pageable: Pageable
    ): HttpEntity<Page<GeneralStatement>> {
        return ok(statementService.findAllBySubject(subjectId, pageable))
    }

    @GetMapping("/subject/{subjectId}/predicate/{predicateId}")
    fun findBySubjectAndPredicate(
        @PathVariable subjectId: String,
        @PathVariable predicateId: PredicateId,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementResponse>> {
        return ok(statementService.findAllBySubjectAndPredicate(subjectId, predicateId, pageable))
    }

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(
        @PathVariable predicateId: PredicateId,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementResponse>> {
        return ok(statementService.findAllByPredicate(predicateId, pageable))
    }

    @GetMapping("/predicate/{predicateId}/literal/{literal}")
    fun findByPredicateAndLiteralAndSubjectClass(
        @PathVariable predicateId: PredicateId,
        @PathVariable literal: String,
        @RequestParam("subjectClass", required = false) subjectClass: ClassId?,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementResponse>> {
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
    ): Page<GeneralStatement> {
        return statementService.findAllByObject(objectId, pageable)
    }

    @GetMapping("/object/{objectId}/predicate/{predicateId}")
    fun findByObjectAndPredicate(
        @PathVariable objectId: String,
        @PathVariable predicateId: PredicateId,
        pageable: Pageable
    ): HttpEntity<Iterable<StatementResponse>> {
        return ok(statementService.findAllByObjectAndPredicate(objectId, predicateId, pageable))
    }

    @Operation(security = [SecurityRequirement(name = "bearer-key")])
    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody statement: CreateStatement,
            uriComponentsBuilder: UriComponentsBuilder,
            principal: Principal):
        HttpEntity<StatementResponse> {
        val userId = authenticatedUserId(principal)
        val body = statementService.create(
            ContributorId(userId!!),
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

    @Operation(security = [SecurityRequirement(name = "bearer-key")])
    @PutMapping("/{id}")
    fun edit(
        @PathVariable id: StatementId,
        @RequestBody(required = true) statementEditRequest: StatementEditRequest
    ): ResponseEntity<StatementResponse> {
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

    @Operation(security = [SecurityRequirement(name = "bearer-key")])
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: StatementId
    ): ResponseEntity<Unit> {
        val foundStatement = statementService.findById(id)

        if (!foundStatement.isPresent)
            return notFound().build()

        statementService.remove(foundStatement.get().id!!)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{thingId}/bundle")
    fun fetchAsBundle(
        @PathVariable thingId: String,
        @RequestParam("minLevel", required = false) minLevel: Int?,
        @RequestParam("maxLevel", required = false) maxLevel: Int?,
        @RequestParam("blacklist", required = false, defaultValue = "") blacklist: List<ClassId>,
        @RequestParam("whitelist", required = false, defaultValue = "") whitelist: List<ClassId>
    ): HttpEntity<Bundle> {
        return ok(
            statementService.fetchAsBundle(
                thingId,
                // FIXME: had to pass configuration like this otherwise lists are not parsed correctly by spring
                BundleConfiguration(
                    minLevel, maxLevel,
                    blacklist, whitelist
                )
            )
        )
    }

    private fun getIdAsString(thing: Thing): String =
        when (thing) {
            is Resource -> thing.id!!.value
            is Literal -> thing.id!!.value
            is Predicate -> thing.id!!.value
            is Class -> thing.id!!.value
            else -> thing.toString()
        }
}

/**
 * A Bundle configuration class containing the min and max levels to be fetched
 * Also the list of classes to be white-listed or black-listed during the fetch
 * @param minLevel the minimum level to be fetched (if not provided it is set to 0)
 * @param maxLevel the maximum level of statements to be fetched (if not provided, all child statements will be fetched)
 * @param blacklist the list of classes to be black-listed (i.e. not fetched), these classes are checked on the subjects and objects of a statement
 * @param whitelist the list of classes to be white-listed (i.e. the only ones to be fetched), these classes are checked on the subjects and objects of a statement
 */
data class BundleConfiguration(
    val minLevel: Int?,
    val maxLevel: Int?,
    val blacklist: List<ClassId>,
    val whitelist: List<ClassId>
) {
    fun toApocConfiguration(): Map<String, Any> {
        val conf = mutableMapOf<String, Any>(
            "relationshipFilter" to ">",
            "bfs" to true
        )
        // configure min and max levels
        if (maxLevel != null)
            conf["maxLevel"] = maxLevel
        if (minLevel != null)
            conf["minLevel"] = minLevel
        // configure blacklisting and whitelisting classes
        var labelFilter = ""
        if (blacklist.isNotEmpty())
            labelFilter = blacklist.joinToString(prefix = "-", separator = "|-")
        if (blacklist.isNotEmpty()) {
            var positiveLabels = whitelist.joinToString(prefix = "+", separator = "|+")
            if (labelFilter.isNotBlank())
                positiveLabels += "|$positiveLabels"
            labelFilter = positiveLabels
        }
        if (blacklist.isNotEmpty() || whitelist.isNotEmpty())
            conf["labelFilter"] = labelFilter
        return conf
    }
}
