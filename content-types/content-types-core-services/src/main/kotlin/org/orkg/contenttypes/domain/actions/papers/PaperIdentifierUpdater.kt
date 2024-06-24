package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.actions.IdentifierUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class PaperIdentifierUpdater(
    private val identifierUpdater: IdentifierUpdater
) : UpdatePaperAction {
    constructor(
        statementService: StatementUseCases,
        literalService: LiteralUseCases
    ) : this(IdentifierUpdater(statementService, literalService))

    override fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.identifiers != null) {
            identifierUpdater.update(
                statements = state.statements,
                contributorId = command.contributorId,
                newIdentifiers = command.identifiers!!,
                identifierDefinitions = Identifiers.paper,
                subjectId = state.paper!!.id
            )
        }
        return state
    }
}
