package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperAuthorListUpdateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : UpdatePaperAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
