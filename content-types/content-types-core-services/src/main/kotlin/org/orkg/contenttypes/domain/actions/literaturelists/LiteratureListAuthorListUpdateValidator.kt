package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.UpdateLiteratureListAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class LiteratureListAuthorListUpdateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : UpdateLiteratureListAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(
        AbstractAuthorListValidator(resourceRepository, statementRepository)
    )

    override fun invoke(command: UpdateLiteratureListCommand, state: State): State {
        if (command.authors != null && command.authors != state.literatureList!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
