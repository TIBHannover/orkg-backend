package org.orkg.widget.domain

import org.orkg.common.exceptions.MissingParameter
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PUBLISHABLE_CLASSES
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.RetrieveResourceUseCase
import org.orkg.graph.input.RetrieveStatementUseCase
import org.orkg.widget.input.ResolveDOIUseCase
import org.orkg.widget.input.ResolveDOIUseCase.WidgetInfo
import org.springframework.stereotype.Service

@Service
class ResolveDOIService(
    private val service: RetrieveResourceUseCase,
    private val statementService: RetrieveStatementUseCase,
) : ResolveDOIUseCase {
    override fun resolveDOI(doi: String?, title: String?): WidgetInfo {
        if (doi != null && title != null) {
            throw TooManyParameters.requiresExactlyOneOf("doi", "title")
        }
        val resource = when {
            doi != null -> service.findByDOI(doi, classes = PUBLISHABLE_CLASSES + Classes.paperVersion)
                .orElseThrow { ResourceNotFound.withDOI(doi) }
            title != null -> service.findPaperByTitle(title).orElseThrow { ResourceNotFound.withLabel(title) }
            else -> throw MissingParameter.requiresAtLeastOneOf("doi", "title")
        }
        val totalStatements =
            if (Classes.paper in resource.classes) {
                statementService.countStatementsInPaperSubgraph(resource.id)
            } else {
                0
            }

        return WidgetInfo(
            id = resource.id,
            doi = doi,
            title = resource.label,
            numberOfStatements = totalStatements,
            `class` = resource.publishableClasses.single().value,
        )
    }
}
