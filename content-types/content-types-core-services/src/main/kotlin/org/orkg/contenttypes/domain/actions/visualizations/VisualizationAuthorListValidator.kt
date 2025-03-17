package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class VisualizationAuthorListValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : VisualizationAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: CreateVisualizationCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
