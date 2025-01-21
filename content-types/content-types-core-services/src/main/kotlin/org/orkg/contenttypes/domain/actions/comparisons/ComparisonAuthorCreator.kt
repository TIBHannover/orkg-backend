package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class ComparisonAuthorCreator(
    unsafeResourceUseCases: UnsafeResourceUseCases,
    statementService: StatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorCreator(unsafeResourceUseCases, statementService, literalService, listService), CreateComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.apply { create(command.contributorId, state.authors, state.comparisonId!!) }
}
