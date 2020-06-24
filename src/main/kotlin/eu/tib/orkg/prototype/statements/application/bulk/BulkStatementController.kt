package eu.tib.orkg.prototype.statements.application.bulk

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.application.StatementController
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.application.StatementResponse
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements")
@CrossOrigin(origins = ["*"])
class BulkStatementController(
    private val statementController: StatementController
) {

    @GetMapping("/subjects")
    fun findBySubjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): ResponseEntity<Iterable<BulkGetStatementsResponse>> {
        return ok(resourceIds.map { BulkGetStatementsResponse(it.value, statementController.findBySubject(it.value, page, items, sortBy, desc).body!!) })
    }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): ResponseEntity<Iterable<BulkGetStatementsResponse>> {
        return ok(resourceIds.map { BulkGetStatementsResponse(it.value, statementController.findByObject(it.value, page, items, sortBy, desc).body!!) })
    }

    @DeleteMapping("/")
    fun delete(
        @RequestParam("ids") statementsIds: List<StatementId>
    ): ResponseEntity<Unit> {
        statementsIds.forEach { statementController.delete(it) }
        return noContent().build()
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