package eu.tib.orkg.prototype.statements.adapter.input.rest.bulk

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.UpdateStatementUseCase
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements")
class BulkStatementController(
    private val statementService: StatementUseCases
) {
    @GetMapping("/subjects")
    fun findBySubjects(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable
    ): ResponseEntity<List<BulkGetStatementsResponse>> {
        return ok(
            resourceIds.map {
                BulkGetStatementsResponse(it, statementService.findAllBySubject(it, pageable))
            }
        )
    }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable
    ): List<BulkGetStatementsResponse> {
        return resourceIds.map { BulkGetStatementsResponse(it, statementService.findAllByObject(it, pageable)) }
}
    @DeleteMapping("/")
    fun delete(
        @RequestParam("ids") statementsIds: List<StatementId>
    ): ResponseEntity<Unit> {
        statementsIds.forEach { statementService.remove(it) }
        return noContent().build()
    }

    @PutMapping("/")
    fun edit(
        @RequestParam("ids") statementsIds: List<StatementId>,
        @RequestBody(required = true) statementEditRequest: BulkStatementEditRequest
    ): ResponseEntity<Iterable<BulkPutStatementResponse>> {
        return ok(statementsIds.map {
            statementService.update(
                UpdateStatementUseCase.UpdateCommand(
                    statementId = it,
                    subjectId = statementEditRequest.subjectId,
                    predicateId = statementEditRequest.predicateId,
                    objectId = statementEditRequest.objectId,
                )
            )
            BulkPutStatementResponse(it, statementService.findById(it).get())
        })
    }
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
