package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class LiteratureListAuthorListUpdateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : UpdateLiteratureListAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: UpdateLiteratureListCommand, state: UpdateLiteratureListState): UpdateLiteratureListState {
        if (command.authors != null && command.authors != state.literatureList!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
