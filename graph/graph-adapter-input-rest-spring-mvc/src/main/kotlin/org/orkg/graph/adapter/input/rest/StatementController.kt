package org.orkg.graph.adapter.input.rest

import java.security.Principal
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.BundleRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.CreateStatement
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementNotFound
import org.orkg.graph.input.BundleRepresentation
import org.orkg.graph.input.StatementRepresentation
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
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
@RequestMapping("/api/statements", produces = [MediaType.APPLICATION_JSON_VALUE])
class StatementController(
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService
) : BaseController(), StatementRepresentationAdapter, BundleRepresentationAdapter {

    @GetMapping("/")
    fun findAll(
        pageable: Pageable
    ): Page<StatementRepresentation> =
        statementService.findAll(pageable).mapToStatementRepresentation()

    @GetMapping("/{statementId}")
    fun findById(@PathVariable statementId: StatementId): StatementRepresentation =
        statementService.findById(statementId).mapToStatementRepresentation().orElseThrow { StatementNotFound(statementId) }

    @GetMapping("/subject/{subjectId}")
    fun findBySubject(
        @PathVariable subjectId: ThingId,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        statementService.findAllBySubject(subjectId, pageable).mapToStatementRepresentation()

    @GetMapping("/subject/{subjectId}/predicate/{predicateId}")
    fun findBySubjectAndPredicate(
        @PathVariable subjectId: ThingId,
        @PathVariable predicateId: ThingId,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        statementService.findAllBySubjectAndPredicate(subjectId, predicateId, pageable).mapToStatementRepresentation()

    @GetMapping("/predicate/{predicateId}")
    fun findByPredicate(
        @PathVariable predicateId: ThingId,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        statementService.findAllByPredicate(predicateId, pageable).mapToStatementRepresentation()

    @GetMapping("/predicate/{predicateId}/literal/{literal}")
    fun findByPredicateAndLiteralAndSubjectClass(
        @PathVariable predicateId: ThingId,
        @PathVariable literal: String,
        @RequestParam("subjectClass", required = false) subjectClass: ThingId?,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        findByPredicateAndLiteralAndSubjectClassWithLiteralAsParameter(predicateId, literal, subjectClass, pageable)

    @GetMapping("/predicate/{predicateId}/literals")
    fun findByPredicateAndLiteralAndSubjectClassWithLiteralAsParameter(
        @PathVariable predicateId: ThingId,
        @RequestParam("q") literal: String,
        @RequestParam("subjectClass", required = false) subjectClass: ThingId?,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        when (subjectClass) {
            null -> statementService.findAllByPredicateAndLabel(predicateId, literal, pageable)
            else -> statementService.findAllByPredicateAndLabelAndSubjectClass(
                predicateId = predicateId,
                literal = literal,
                subjectClass = subjectClass,
                pagination = pageable
            )
        }.mapToStatementRepresentation()

    @GetMapping("/object/{objectId}")
    fun findByObject(
        @PathVariable objectId: ThingId,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        statementService.findAllByObject(objectId, pageable).mapToStatementRepresentation()

    @GetMapping("/object/{objectId}/predicate/{predicateId}")
    fun findByObjectAndPredicate(
        @PathVariable objectId: ThingId,
        @PathVariable predicateId: ThingId,
        pageable: Pageable
    ): Page<StatementRepresentation> =
        statementService.findAllByObjectAndPredicate(objectId, predicateId, pageable).mapToStatementRepresentation()

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody statement: CreateStatement,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<StatementRepresentation> {
        val userId = authenticatedUserId()
        val id = statementService.create(
            ContributorId(userId),
            statement.subjectId,
            statement.predicateId,
            statement.objectId
        )
        val location = uriComponentsBuilder
            .path("api/statements/{id}")
            .buildAndExpand(statement.id)
            .toUri()
        return created(location).body(statementService.findById(id).mapToStatementRepresentation().get())
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun edit(
        @PathVariable id: StatementId,
        @RequestBody(required = true) statementEditRequest: StatementEditRequest
    ): ResponseEntity<StatementRepresentation> {
        val foundStatement = statementService.findById(id)

        if (!foundStatement.isPresent)
            return notFound().build()

        statementService.update(
            UpdateStatementUseCase.UpdateCommand(
                statementId = id,
                subjectId = statementEditRequest.subjectId ?: foundStatement.get().subject.id,
                predicateId = statementEditRequest.predicateId ?: foundStatement.get().predicate.id,
                objectId = statementEditRequest.objectId ?: foundStatement.get().`object`.id
            )
        )

        return statementService.findById(id).mapToStatementRepresentation().map(::ok).get()
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: StatementId,
        principal: Principal?
    ): ResponseEntity<Unit> {
        if (principal?.name == null)
            return ResponseEntity(HttpStatus.FORBIDDEN)
        statementService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{thingId}/bundle")
    fun fetchAsBundle(
        @PathVariable thingId: ThingId,
        @RequestParam("minLevel", required = false) minLevel: Int?,
        @RequestParam("maxLevel", required = false) maxLevel: Int?,
        @RequestParam("blacklist", required = false, defaultValue = "") blacklist: List<ThingId>,
        @RequestParam("whitelist", required = false, defaultValue = "") whitelist: List<ThingId>,
        @RequestParam("includeFirst", required = false, defaultValue = "true") includeFirst: Boolean,
        sort: Sort
    ): BundleRepresentation =
        statementService.fetchAsBundle(
            thingId,
            // FIXME: had to pass configuration like this otherwise lists are not parsed correctly by spring
            BundleConfiguration(
                minLevel, maxLevel,
                blacklist, whitelist
            ),
            includeFirst,
            sort
        ).toBundleRepresentation()
}