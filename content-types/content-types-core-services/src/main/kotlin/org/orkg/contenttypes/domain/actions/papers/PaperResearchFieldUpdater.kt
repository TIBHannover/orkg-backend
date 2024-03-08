package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.ResearchFieldUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.StatementUseCases

class PaperResearchFieldUpdater(
    statementService: StatementUseCases
) : ResearchFieldUpdater(statementService), UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.researchFields != null && command.researchFields != state.paper!!.researchFields) {
            update(command.contributorId, command.researchFields!!, command.paperId)
        }
        return state
    }
}
