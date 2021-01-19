package eu.tib.orkg.prototype.statements.adapter.input.rest.bulk

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.application.bulk.BulkGetStatementsResponse
import eu.tib.orkg.prototype.statements.application.port.`in`.GetBulkStatementsQuery
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements/new")
class BulkStatementController(
    private val query: GetBulkStatementsQuery
) {

    @GetMapping("/subjects")
    fun findBySubjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): ResponseEntity<Iterable<BulkGetStatementsResponse>> {
        val pageable = createPageable(page, items, sortBy, desc)
        return ok(query.getBulkStatementsBySubjects(resourceIds.map { it.value }, pageable)
            .map { (k, v) -> BulkGetStatementsResponse(k, v) })
    }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ResourceId>,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): ResponseEntity<Iterable<BulkGetStatementsResponse>> {
        val pageable = createPageable(page, items, sortBy, desc)
        return ok(query.getBulkStatementsByObjects(resourceIds.map { it.value }, pageable)
            .map { (k, v) -> BulkGetStatementsResponse(k, v) })
    }
}
