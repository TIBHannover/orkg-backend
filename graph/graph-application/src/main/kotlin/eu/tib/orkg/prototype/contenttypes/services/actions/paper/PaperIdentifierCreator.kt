package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.identifiers.domain.parse
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class PaperIdentifierCreator(
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val identifiers = Identifiers.paper.parse(command.identifiers, validate = false)
        identifiers.forEach { (identifier, value) ->
            statementService.create(state.paperId!!, identifier.predicateId, literalService.create(value).id)
        }
        return state
    }
}
