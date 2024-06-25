package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.output.ResourceRepository

class LiteratureListExistenceValidator(
    private val abstractLiteratureListExistenceValidator: AbstractLiteratureListExistenceValidator
) : UpdateLiteratureListAction {
    constructor(
        literatureListService: LiteratureListService,
        resourceRepository: ResourceRepository
    ) : this(AbstractLiteratureListExistenceValidator(literatureListService, resourceRepository))

    override fun invoke(
        command: UpdateLiteratureListCommand,
        state: UpdateLiteratureListState
    ): UpdateLiteratureListState =
        abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(command.literatureListId)
            .let { state.copy(literatureList = it.first, statements = it.second) }
}
