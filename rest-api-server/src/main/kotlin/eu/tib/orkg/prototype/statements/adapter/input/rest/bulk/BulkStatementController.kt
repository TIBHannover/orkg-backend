package eu.tib.orkg.prototype.statements.adapter.input.rest.bulk

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.application.StatementResponse
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
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
        @RequestParam("ids") resourceIds: List<ResourceId>,
        pageable: Pageable
    ): ResponseEntity<List<BulkGetStatementsResponse>> {
        return ok(
                resourceIds.map {
                        BulkGetStatementsResponse(it.value,
                            statementService.findAllBySubject(it.value, pageable)
                        )
                }
        )
    }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        pageable: Pageable
    ): List<BulkGetStatementsResponse> {
        return resourceIds.map { BulkGetStatementsResponse(it.value, statementService.findAllByObject(it.value, pageable)) }
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
        @RequestBody(required = true) statementEditRequest: StatementEditRequest
    ): ResponseEntity<Iterable<BulkPutStatementResponse>> {
        return ok(statementsIds.map { BulkPutStatementResponse(it, statementService.update(statementEditRequest)) })
    }
}

data class BulkGetStatementsResponse(
    val id: String,
    val statements: Page<GeneralStatement>
)

data class BulkPutStatementResponse(
    @JsonProperty("id")
    val statementId: StatementId,
    val statement: StatementResponse
)
