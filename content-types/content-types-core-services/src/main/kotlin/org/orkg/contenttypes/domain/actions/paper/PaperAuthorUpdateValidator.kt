package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperAuthorUpdateValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState =
        command.authors?.let { state.copy(authors = validate(it)) } ?: state
}
