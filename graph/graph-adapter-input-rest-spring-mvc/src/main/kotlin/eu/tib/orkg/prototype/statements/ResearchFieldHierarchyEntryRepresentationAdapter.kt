package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.ResearchFieldHierarchyEntryRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.*
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import org.springframework.data.domain.Page

interface ResearchFieldHierarchyEntryRepresentationAdapter : ResourceRepresentationAdapter {

    fun Page<ResearchFieldHierarchyEntry>.mapToResearchFieldHierarchyEntryRepresentation(): Page<ResearchFieldHierarchyEntryRepresentation> {
        val resources = content.map { it.resource }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toResearchFieldHierarchyEntryRepresentation(usageCounts, formattedLabels) }
    }

    fun ResearchFieldHierarchyEntry.toResearchFieldHierarchyEntryRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): ResearchFieldHierarchyEntryRepresentation =
        ResearchFieldHierarchyEntryRepresentation(resource.toResourceRepresentation(usageCounts, formattedLabels), parentIds)
}
