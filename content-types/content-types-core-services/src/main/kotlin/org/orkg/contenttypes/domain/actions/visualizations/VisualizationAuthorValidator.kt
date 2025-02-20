package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class VisualizationAuthorValidator(
    private val authorValidator: AuthorValidator,
) : VisualizationAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: CreateVisualizationCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
