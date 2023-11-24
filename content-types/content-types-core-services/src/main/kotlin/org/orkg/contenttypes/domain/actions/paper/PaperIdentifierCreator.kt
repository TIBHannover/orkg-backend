package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

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
