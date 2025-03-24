package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.IdentifierUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.contenttypes.domain.identifiers.Identifiers
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperIdentifierUpdater(
    private val identifierUpdater: IdentifierUpdater,
) : UpdatePaperAction {
    constructor(
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        IdentifierUpdater(statementService, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: UpdatePaperCommand, state: State): State {
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
