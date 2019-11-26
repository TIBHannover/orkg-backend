package eu.tib.orkg.prototype.statements.application.bulk

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.application.StatementController
import eu.tib.orkg.prototype.statements.application.StatementResponse
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
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
        @RequestParam("ids") resourceIds: List<ResourceId>
    ): ResponseEntity<Iterable<BulkStatementResponse>> {
        val page = 1
        val items = 99999
        val by = "id"
        val sort = false
        return ok(resourceIds.map { BulkStatementResponse(it.value, statementController.findByResource(it, page, items, by, sort).body!!) })
    }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ResourceId>
    ): ResponseEntity<Iterable<BulkStatementResponse>> {
        val page = 1
        val items = 99999
        val by = "id"
        val sort = false
        return ok(resourceIds.map { BulkStatementResponse(it.value, statementController.findByObject(it.value, page, items, by, sort).body!!) })
    }
}

data class BulkStatementResponse(
    @JsonProperty("resource_id")
    val resourceId: String,
    val statements: Iterable<StatementResponse>
)
