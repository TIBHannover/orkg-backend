package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class LiteratureListAuthorListCreateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : CreateLiteratureListAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(
        AbstractAuthorListValidator(resourceRepository, statementRepository)
    )

    override fun invoke(command: CreateLiteratureListCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
