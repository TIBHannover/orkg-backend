package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonAuthorValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.copy(authors = validate(command.authors))
}
