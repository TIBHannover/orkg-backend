package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ResearchFieldCreator
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class PaperResearchFieldCreator(
    statementService: StatementUseCases
) : ResearchFieldCreator(statementService), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperAction.State): PaperAction.State {
        create(command.contributorId, command.researchFields, state.paperId!!)
        return state
    }
}
