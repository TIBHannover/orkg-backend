package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.ResearchFieldWithChildCountRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldWithChildCount
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import org.springframework.data.domain.Page

interface ResearchFieldWithChildCountRepresentationAdapter : ResourceRepresentationAdapter {

    fun Page<ResearchFieldWithChildCount>.mapToResearchFieldWithChildCountRepresentation(): Page<ResearchFieldWithChildCountRepresentation> {
        val resources = content.map { it.resource }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toResearchFieldWithChildCountRepresentation(usageCounts, formattedLabels) }
    }

    fun ResearchFieldWithChildCount.toResearchFieldWithChildCountRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): ResearchFieldWithChildCountRepresentation =
        ResearchFieldWithChildCountRepresentation(resource.toResourceRepresentation(usageCounts, formattedLabels), childCount)
}
