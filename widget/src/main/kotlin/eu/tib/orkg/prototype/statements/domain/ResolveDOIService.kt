package eu.tib.orkg.prototype.statements.domain

import eu.tib.orkg.prototype.shared.MissingParameter
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.api.ResolveDOIUseCase
import eu.tib.orkg.prototype.statements.api.ResolveDOIUseCase.WidgetInfo
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.publishableClasses
import org.springframework.stereotype.Service

@Service
class ResolveDOIService(
    private val service: ResourceUseCases,
    private val statementService: StatementUseCases,
) : ResolveDOIUseCase {
    override fun resolveDOI(doi: String?, title: String?): WidgetInfo {
        if (doi != null && title != null)
            throw TooManyParameters.requiresExactlyOneOf("doi", "title")
        val resource = when {
            doi != null -> service.findByDOI(doi).orElseThrow { ResourceNotFound.withDOI(doi) }
            title != null -> service.findByTitle(title).orElseThrow { ResourceNotFound.withLabel(title) }
            else -> throw MissingParameter.requiresAtLeastOneOf("doi", "title")
        }
        val totalStatements =
            if (ThingId("Paper") in resource.classes)
                statementService.countStatements(resource.id)
            else
                0

        return WidgetInfo(
            id = resource.id,
            doi = doi,
            title = resource.label,
            numberOfStatements = totalStatements,
            `class` = (resource.classes intersect publishableClasses).single().value,
        )
    }
}
