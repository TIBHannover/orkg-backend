package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.graph.adapter.input.rest.ResearchFieldWithChildCountRepresentation
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.springframework.data.domain.Page

interface ResearchFieldWithChildCountRepresentationAdapter : ResourceRepresentationAdapter {
    fun Page<ResearchFieldWithChildCount>.mapToResearchFieldWithChildCountRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<ResearchFieldWithChildCountRepresentation> {
        val resources = content.map { it.resource }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toResearchFieldWithChildCountRepresentation(usageCounts, formattedLabels) }
    }

    fun ResearchFieldWithChildCount.toResearchFieldWithChildCountRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
    ): ResearchFieldWithChildCountRepresentation =
        ResearchFieldWithChildCountRepresentation(resource.toResourceRepresentation(usageCounts, formattedLabels), childCount)
}
