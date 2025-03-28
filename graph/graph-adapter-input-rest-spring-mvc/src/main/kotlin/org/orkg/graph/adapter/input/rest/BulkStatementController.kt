package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.pmap
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
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
) : StatementRepresentationAdapter {
    @GetMapping("/subjects")
    fun findAllBySubjectId(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): List<BulkStatementRepresentation> =
        resourceIds.pmap {
            BulkStatementRepresentation(
                id = it,
                statements = statementService.findAll(subjectId = it, pageable = pageable)
                    .mapToStatementRepresentation(capabilities)
            )
        }

    @GetMapping("/objects")
    fun findAllByObjectId(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): List<BulkStatementRepresentation> =
        resourceIds.pmap {
            BulkStatementRepresentation(
                id = it,
                statements = statementService.findAll(objectId = it, pageable = pageable)
                    .mapToStatementRepresentation(capabilities)
            )
        }

    @RequireLogin
    @DeleteMapping
    fun delete(
        @RequestParam("ids") statementsIds: Set<StatementId>,
    ): ResponseEntity<Unit> {
        statementService.deleteAllById(statementsIds)
        return noContent().build()
    }

    @RequireLogin
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @RequestParam("ids") statementsIds: List<StatementId>,
        @RequestBody(required = true) statementEditRequest: BulkStatementUpdateRequest,
        currentUser: Authentication?,
        capabilities: MediaTypeCapabilities,
    ): Iterable<BulkPutStatementResponse> =
        statementsIds.map {
            statementService.update(
                UpdateStatementUseCase.UpdateCommand(
                    statementId = it,
                    contributorId = currentUser.contributorId(),
                    subjectId = statementEditRequest.subjectId,
                    predicateId = statementEditRequest.predicateId,
                    objectId = statementEditRequest.objectId,
                )
            )
            statementService.findById(it).get()
        }
            .mapToStatementRepresentation(capabilities)
            .map { BulkPutStatementResponse(it.id, it) }

    data class BulkStatementUpdateRequest(
        @JsonProperty("subject_id")
        val subjectId: ThingId? = null,
        @JsonProperty("predicate_id")
        val predicateId: ThingId? = null,
        @JsonProperty("object_id")
        val objectId: ThingId? = null,
    )
}

@Deprecated("To be removed")
data class BulkPutStatementResponse(
    @JsonProperty("id")
    val statementId: StatementId,
    val statement: StatementRepresentation,
)
