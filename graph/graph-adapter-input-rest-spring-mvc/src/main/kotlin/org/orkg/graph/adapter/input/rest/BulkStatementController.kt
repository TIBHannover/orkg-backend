package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.contenttypes.domain.pmap
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements", produces = [MediaType.APPLICATION_JSON_VALUE])
class BulkStatementController(
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
    override val flags: FeatureFlagService
) : StatementRepresentationAdapter {

    @GetMapping("/subjects")
    fun findBySubjects(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): List<BulkGetStatementsResponse> =
        resourceIds.pmap {
            BulkGetStatementsResponse(
                id = it,
                statements = statementService.findAll(subjectId = it, pageable = pageable)
                    .mapToStatementRepresentation(capabilities)
            )
        }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): List<BulkGetStatementsResponse> =
        resourceIds.pmap {
            BulkGetStatementsResponse(
                id = it,
                statements = statementService.findAll(objectId = it, pageable = pageable)
                    .mapToStatementRepresentation(capabilities)
            )
        }

    @PreAuthorizeUser
    @DeleteMapping
    fun delete(
        @RequestParam("ids") statementsIds: Set<StatementId>
    ): ResponseEntity<Unit> {
        statementService.delete(statementsIds)
        return noContent().build()
    }

    @PreAuthorizeUser
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun edit(
        @RequestParam("ids") statementsIds: List<StatementId>,
        @RequestBody(required = true) statementEditRequest: BulkStatementEditRequest,
        capabilities: MediaTypeCapabilities
    ): Iterable<BulkPutStatementResponse> =
        statementsIds.map {
            statementService.update(
                UpdateStatementUseCase.UpdateCommand(
                    statementId = it,
                    subjectId = statementEditRequest.subjectId,
                    predicateId = statementEditRequest.predicateId,
                    objectId = statementEditRequest.objectId,
                )
            )
            statementService.findById(it).get()
        }
            .mapToStatementRepresentation(capabilities)
            .map { BulkPutStatementResponse(it.id, it) }
}

data class BulkGetStatementsResponse(
    val id: ThingId,
    val statements: Page<StatementRepresentation>
)

data class BulkPutStatementResponse(
    @JsonProperty("id")
    val statementId: StatementId,
    val statement: StatementRepresentation
)

data class BulkStatementEditRequest(
    @JsonProperty("subject_id")
    val subjectId: ThingId? = null,

    @JsonProperty("predicate_id")
    val predicateId: ThingId? = null,

    @JsonProperty("object_id")
    val objectId: ThingId? = null
)
