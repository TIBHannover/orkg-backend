package org.orkg.contenttypes.domain.actions.visualization

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class VisualizationAuthorValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State =
        state.copy(authors = validate(command.authors))
}
