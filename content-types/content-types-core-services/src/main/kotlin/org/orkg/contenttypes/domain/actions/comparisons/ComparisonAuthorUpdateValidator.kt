package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonAuthorUpdateValidator(
    private val authorValidator: AuthorValidator,
) : UpdateComparisonAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: UpdateComparisonCommand, state: UpdateComparisonState): UpdateComparisonState {
        if (command.authors != null && command.authors != state.comparison!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
