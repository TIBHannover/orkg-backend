package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.adapter.input.rest.ResearchFieldHierarchyEntryRepresentation
import org.springframework.data.domain.Page

interface ResearchFieldHierarchyEntryRepresentationAdapter : ResourceRepresentationAdapter {

    fun Page<ResearchFieldHierarchyEntry>.mapToResearchFieldHierarchyEntryRepresentation(): Page<ResearchFieldHierarchyEntryRepresentation> {
        val resources = content.map { it.resource }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toResearchFieldHierarchyEntryRepresentation(usageCounts, formattedLabels) }
    }

    fun ResearchFieldHierarchyEntry.toResearchFieldHierarchyEntryRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): ResearchFieldHierarchyEntryRepresentation =
        ResearchFieldHierarchyEntryRepresentation(resource.toResourceRepresentation(usageCounts, formattedLabels), parentIds)
}
