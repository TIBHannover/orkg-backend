package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.ResearchFieldCreator
import org.orkg.graph.input.StatementUseCases

class PaperResearchFieldCreator(
    statementService: StatementUseCases
) : ResearchFieldCreator(statementService), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperAction.State): PaperAction.State {
        create(command.contributorId, command.researchFields, state.paperId!!)
        return state
    }
}
