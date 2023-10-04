package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class PaperIdentifierCreator(
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val identifiers = Identifiers.paper associateWith command.identifiers
        // TODO: Do we want to validate identifier values structurally?
        identifiers.forEach { (predicate, value) ->
            statementService.create(state.paperId!!, predicate, literalService.create(value).id)
        }
        return state
    }
}
