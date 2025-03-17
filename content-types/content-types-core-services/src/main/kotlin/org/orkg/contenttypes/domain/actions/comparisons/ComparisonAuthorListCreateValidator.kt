package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonAuthorListCreateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : CreateComparisonAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
