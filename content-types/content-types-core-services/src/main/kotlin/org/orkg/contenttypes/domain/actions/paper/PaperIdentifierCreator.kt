package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.IdentifierCreator
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class PaperIdentifierCreator(
    statementService: StatementUseCases,
    literalService: LiteralUseCases
) : IdentifierCreator(statementService, literalService), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.apply { create(command.contributorId, command.identifiers, Identifiers.paper, state.paperId!!) }
}
