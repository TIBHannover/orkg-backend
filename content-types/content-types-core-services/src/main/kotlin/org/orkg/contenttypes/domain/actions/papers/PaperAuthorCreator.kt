package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperAuthorCreator(
    unsafeResourceUseCases: UnsafeResourceUseCases,
    unsafeStatementUseCases: UnsafeStatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, literalService, listService), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.apply { create(command.contributorId, state.authors, state.paperId!!) }
}
