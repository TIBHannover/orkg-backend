package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.ResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.StatementUseCases

class PaperResearchFieldUpdater(
    statementService: StatementUseCases
) : ResearchFieldUpdater(statementService), UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.researchFields != null) {
            update(command.contributorId, command.researchFields!!, command.paperId)
        }
        return state
    }
}
