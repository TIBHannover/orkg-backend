package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.IdentifierCreator
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.contenttypes.domain.identifiers.Identifiers
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperIdentifierCreator(
    unsafeStatementUseCases: UnsafeStatementUseCases,
    unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : IdentifierCreator(unsafeStatementUseCases, unsafeLiteralUseCases),
    CreatePaperAction {
    override fun invoke(command: CreatePaperCommand, state: State): State =
        state.apply { create(command.contributorId, command.identifiers, Identifiers.paper, paperId!!) }
}
