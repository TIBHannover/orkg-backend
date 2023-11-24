package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.paper.PaperAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperAuthorValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State =
        state.copy(authors = validate(command.authors))
}
