package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonAuthorListUpdateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : UpdateComparisonAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: UpdateComparisonCommand, state: UpdateComparisonState): UpdateComparisonState {
        if (command.authors != null && command.authors != state.comparison!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
