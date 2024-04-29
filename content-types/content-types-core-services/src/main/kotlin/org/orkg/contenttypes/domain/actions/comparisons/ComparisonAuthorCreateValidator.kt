package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonAuthorCreateValidator(
    private val authorValidator: AuthorValidator
) : CreateComparisonAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
