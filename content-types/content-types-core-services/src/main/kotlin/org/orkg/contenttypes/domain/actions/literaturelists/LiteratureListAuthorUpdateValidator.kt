package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class LiteratureListAuthorUpdateValidator(
    private val authorValidator: AuthorValidator
) : UpdateLiteratureListAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: UpdateLiteratureListCommand, state: UpdateLiteratureListState): UpdateLiteratureListState {
        if (command.authors != null && command.authors != state.literatureList!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
