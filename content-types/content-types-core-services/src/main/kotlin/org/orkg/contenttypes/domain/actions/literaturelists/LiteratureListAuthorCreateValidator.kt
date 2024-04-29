package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class LiteratureListAuthorCreateValidator(
    private val authorValidator: AuthorValidator
) : CreateLiteratureListAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override operator fun invoke(command: CreateLiteratureListCommand, state: CreateLiteratureListState): CreateLiteratureListState =
        state.copy(authors = authorValidator.validate(command.authors))
}
