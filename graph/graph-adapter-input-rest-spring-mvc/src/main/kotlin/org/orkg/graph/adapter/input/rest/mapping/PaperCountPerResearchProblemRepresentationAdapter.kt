package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.adapter.input.rest.PaperCountPerResearchProblemRepresentation
import org.springframework.data.domain.Page

interface PaperCountPerResearchProblemRepresentationAdapter : ResourceRepresentationAdapter {

    fun Page<PaperCountPerResearchProblem>.mapToPaperCountPerResearchProblemRepresentation(): Page<PaperCountPerResearchProblemRepresentation> {
        val resources = content.map { it.problem }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toPaperCountPerResearchProblemRepresentation(usageCounts, formattedLabels) }
    }

    fun PaperCountPerResearchProblem.toPaperCountPerResearchProblemRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): PaperCountPerResearchProblemRepresentation =
        PaperCountPerResearchProblemRepresentation(problem.toResourceRepresentation(usageCounts, formattedLabels), papers)
}
