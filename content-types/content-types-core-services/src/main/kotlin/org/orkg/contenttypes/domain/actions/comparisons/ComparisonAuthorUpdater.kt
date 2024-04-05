package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonAuthorUpdater(
    private val authorUpdater: AuthorUpdater
) : UpdateComparisonAction {
    constructor(
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
        literalService: LiteralUseCases,
        listService: ListUseCases
    ) : this(AuthorUpdater(resourceService, statementService, literalService, listService))

    override operator fun invoke(command: UpdateComparisonCommand, state: UpdateComparisonState): UpdateComparisonState {
        if (command.authors != null && command.authors != state.comparison!!.authors) {
            authorUpdater.update(command.contributorId, state.authors, command.comparisonId)
        }
        return state
    }
}
