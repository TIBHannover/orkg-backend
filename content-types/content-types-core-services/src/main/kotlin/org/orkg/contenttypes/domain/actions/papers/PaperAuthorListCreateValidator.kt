package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperAuthorListCreateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : CreatePaperAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.copy(authors = authorValidator.validate(command.authors))
}
