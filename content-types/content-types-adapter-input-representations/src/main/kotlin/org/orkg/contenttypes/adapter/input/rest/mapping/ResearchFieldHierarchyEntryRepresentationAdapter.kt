package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.graph.adapter.input.rest.ResearchFieldHierarchyEntryRepresentation
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.springframework.data.domain.Page

interface ResearchFieldHierarchyEntryRepresentationAdapter : ResourceRepresentationAdapter {
    fun Page<ResearchFieldHierarchyEntry>.mapToResearchFieldHierarchyEntryRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<ResearchFieldHierarchyEntryRepresentation> {
        val resources = content.map { it.resource }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toResearchFieldHierarchyEntryRepresentation(usageCounts, formattedLabels) }
    }

    fun ResearchFieldHierarchyEntry.toResearchFieldHierarchyEntryRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
    ): ResearchFieldHierarchyEntryRepresentation =
        ResearchFieldHierarchyEntryRepresentation(resource.toResourceRepresentation(usageCounts, formattedLabels), parentIds)
}
