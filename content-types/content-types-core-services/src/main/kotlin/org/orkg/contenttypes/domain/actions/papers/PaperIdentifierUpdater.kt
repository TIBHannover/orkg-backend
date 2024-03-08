package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.actions.IdentifierUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class PaperIdentifierUpdater(
    statementService: StatementUseCases,
    literalService: LiteralUseCases
) : IdentifierUpdater(statementService, literalService), UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.identifiers != null) {
            update(
                contributorId = command.contributorId,
                oldIdentifiers = state.paper!!.identifiers,
                newIdentifiers = command.identifiers!!,
                identifierDefinitions = Identifiers.paper,
                subjectId = state.paper.id
            )
        }
        return state
    }
}
