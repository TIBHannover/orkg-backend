package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/widgets/")
class WidgetController(
    private val service: ResourceUseCases,
    private val statementService: StatementUseCases
) {
    @GetMapping("/")
    fun searchDoi(
        @RequestParam(required = false) doi: String?,
        @RequestParam(required = false) title: String?
    ): WidgetInfo {
        if (doi != null && title != null)
            throw TooManyParameters.requiresExactlyOneOf("doi", "title")
        val resource = when {
            doi != null -> service.findByDOI(doi).orElseThrow { ResourceNotFound.withDOI(doi) }
            title != null -> service.findByTitle(title).orElseThrow { ResourceNotFound.withLabel(title) }
            else -> throw MissingParameter.requiresAtLeastOneOf("doi", "title")
        }
        val totalStatements = statementService.countStatements(resource.id)

        return WidgetInfo(
            id = resource.id,
            doi = doi,
            title = resource.label,
            numberOfStatements = totalStatements
        )
    }

    data class WidgetInfo(
        val id: ThingId,
        val doi: String?,
        val title: String,
        @JsonProperty("num_statements")
        val numberOfStatements: Long
    )
}
