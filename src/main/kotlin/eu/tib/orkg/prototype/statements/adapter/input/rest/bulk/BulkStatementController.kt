package eu.tib.orkg.prototype.statements.adapter.input.rest.bulk

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.application.StatementController
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.application.StatementResponse
import eu.tib.orkg.prototype.statements.application.port.`in`.GetBulkStatementsQuery
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
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
    private val query: GetBulkStatementsQuery,
    private val statementController: StatementController
) {

    @GetMapping("/subjects")
    fun findBySubjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        pageable: Pageable
    ): ResponseEntity<Iterable<BulkGetStatementsResponse>> {
        return ok(query.getBulkStatementsBySubjects(resourceIds.map { it.value }, pageable)
            .map { (k, v) -> BulkGetStatementsResponse(k, v) })}

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        pageable: Pageable
    ): ResponseEntity<Iterable<BulkGetStatementsResponse>> {
        return ok(query.getBulkStatementsByObjects(resourceIds.map { it.value }, pageable)
            .map { (k, v) -> BulkGetStatementsResponse(k, v) })
    }

    @DeleteMapping("/")
    fun delete(
        @RequestParam("ids") statementsIds: List<StatementId>
    ): ResponseEntity<Unit> {
        statementsIds.forEach { statementController.delete(it) }
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/")
    fun edit(
        @RequestParam("ids") statementsIds: List<StatementId>,
        @RequestBody(required = true) statementEditRequest: StatementEditRequest
    ): ResponseEntity<Iterable<BulkPutStatementResponse>> {
        return ok(statementsIds.map { BulkPutStatementResponse(it, statementController.edit(it, statementEditRequest).body!!) })
    }
}

data class BulkGetStatementsResponse(
    val id: String,
    val statements: Iterable<StatementResponse>
)

data class BulkPutStatementResponse(
    @JsonProperty("id")
    val statementId: StatementId,
    val statement: StatementResponse
)
