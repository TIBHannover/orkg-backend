package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperAuthorUpdateValidator(
    private val authorValidator: AuthorValidator
) : UpdatePaperAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
