package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AbstractAuthorListUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class ComparisonAuthorListUpdater(
    private val authorUpdater: AbstractAuthorListUpdater,
) : UpdateComparisonAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
        listRepository: ListRepository,
    ) : this(
        AbstractAuthorListUpdater(
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            listService,
            listRepository
        )
    )

    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.authors != null && command.authors != state.comparison!!.authors) {
            authorUpdater.update(state.statements, command.contributorId, state.authors, command.comparisonId)
        }
        return state
    }
}
