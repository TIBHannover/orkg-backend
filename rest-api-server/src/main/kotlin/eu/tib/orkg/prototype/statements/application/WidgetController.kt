package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/widgets/")
class WidgetController(private val service: ResourceUseCases, private val statementService: StatementService) {

    @GetMapping("/")
    fun searchDoi(
        @RequestParam("doi", required = false) searchString: String?,
        @RequestParam("title", required = false) titleString: String?
    ): HttpEntity<WidgetInfo> {

        require(searchString != null || titleString != null) { "doi and title is missing" }
        val found = (if (searchString != null)
            service.findAllByDOI(searchString).firstOrNull()
        else
            service.findAllByTitle(titleString).firstOrNull())
            ?: return notFound().build()

        val totalStatements = statementService.countStatements(found.id!!.value)

        return ok(WidgetInfo(id = found.id.toString(),
                                doi = searchString,
                                title = found.label,
                                numberOfStatements = totalStatements))
    }

    data class WidgetInfo(
        val id: String,
        val doi: String?,
        val title: String,
        @JsonProperty("num_statements")
        val numberOfStatements: Int
    )
}
