package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AbstractAuthorListCreator
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonAuthorListCreator(
    private val authorCreator: AbstractAuthorListCreator,
) : CreateComparisonAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
    ) : this(
        AbstractAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
    )

    override fun invoke(command: CreateComparisonCommand, state: State): State =
        state.apply { authorCreator.create(command.contributorId, authors, comparisonId!!) }
}
