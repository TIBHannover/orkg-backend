package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ResearchFieldCreator
import org.orkg.graph.input.StatementUseCases

class PaperResearchFieldCreator(
    statementService: StatementUseCases
) : ResearchFieldCreator(statementService), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.apply { create(command.contributorId, command.researchFields, state.paperId!!) }
}
