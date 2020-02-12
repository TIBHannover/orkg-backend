package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/widgets/")
@CrossOrigin(origins = ["*"])
class WidgetController(private val service: ResourceService, private val statementService: StatementService) {

    @GetMapping("/")
    fun searchDoi(
        @RequestParam("doi", required = false) searchString: String?,
        @RequestParam("title", required = false) titleString: String?
    ): HttpEntity<WidgetInfo> {

        require(searchString != null || titleString != null) { "doi and title is missing" }
        val found = if (searchString != null)
            service.findByDOI(searchString)
        else
            service.findByTitle(titleString)

        val paperNode = found.orElseThrow { ResourceNotFound() }

        val totalStatements = statementService.countStatements(paperNode.id!!.value)

        return ok(WidgetInfo(doi = searchString,
                                title = paperNode.label,
                                numberOfStatements = totalStatements))
    }

    data class WidgetInfo(
        val doi: String?,
        val title: String,
        @JsonProperty("num_statements")
        val numberOfStatements: Int
    )
}
