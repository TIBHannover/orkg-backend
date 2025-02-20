package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.graph.adapter.input.rest.PaperCountPerResearchProblemRepresentation
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.StatementCounts
import org.springframework.data.domain.Page

interface PaperCountPerResearchProblemRepresentationAdapter : ResourceRepresentationAdapter {
    fun Page<PaperCountPerResearchProblem>.mapToPaperCountPerResearchProblemRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<PaperCountPerResearchProblemRepresentation> {
        val resources = content.map { it.problem }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toPaperCountPerResearchProblemRepresentation(usageCounts, formattedLabels) }
    }

    fun PaperCountPerResearchProblem.toPaperCountPerResearchProblemRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
    ): PaperCountPerResearchProblemRepresentation =
        PaperCountPerResearchProblemRepresentation(problem.toResourceRepresentation(usageCounts, formattedLabels), papers)
}
