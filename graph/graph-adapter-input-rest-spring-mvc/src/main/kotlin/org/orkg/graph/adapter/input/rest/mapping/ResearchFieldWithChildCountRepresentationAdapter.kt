package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.input.ResearchFieldWithChildCountRepresentation
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
