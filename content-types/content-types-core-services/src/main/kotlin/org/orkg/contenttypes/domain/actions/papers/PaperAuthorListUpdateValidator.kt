package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperAuthorListUpdateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : UpdatePaperAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(
        AbstractAuthorListValidator(resourceRepository, statementRepository)
    )

    override fun invoke(command: UpdatePaperCommand, state: State): State {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
