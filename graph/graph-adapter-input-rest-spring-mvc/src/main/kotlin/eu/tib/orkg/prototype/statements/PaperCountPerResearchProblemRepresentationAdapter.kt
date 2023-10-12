package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.PaperCountPerResearchProblemRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase.PaperCountPerResearchProblem
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
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
