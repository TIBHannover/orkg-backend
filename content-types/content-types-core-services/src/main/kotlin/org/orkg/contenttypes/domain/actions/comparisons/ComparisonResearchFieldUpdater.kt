package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.ResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class ComparisonResearchFieldUpdater(
    private val researchFieldUpdater: ResearchFieldUpdater
) : UpdateComparisonAction {
    constructor(statementService: StatementUseCases) : this(object : ResearchFieldUpdater(statementService) {})

    override operator fun invoke(command: UpdateComparisonCommand, state: UpdateComparisonState): UpdateComparisonState {
        if (command.researchFields != null && command.researchFields != state.comparison!!.researchFields.map { it.id }) {
            researchFieldUpdater.update(
                contributorId = command.contributorId,
                researchFields = command.researchFields!!,
                subjectId = command.comparisonId,
                predicateId = Predicates.hasSubject
            )
        }
        return state
    }
}
