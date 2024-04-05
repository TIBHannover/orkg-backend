package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonAuthorCreateValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), CreateComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.copy(authors = validate(command.authors))
}
