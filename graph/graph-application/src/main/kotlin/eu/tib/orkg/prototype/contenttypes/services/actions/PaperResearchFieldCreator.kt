package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class PaperResearchFieldCreator(
    private val statementService: StatementUseCases,
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        command.researchFields.distinct().forEach {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasResearchField,
                `object` = it
            )
        }
        return state
    }
}
