package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListExistenceValidator
import org.orkg.contenttypes.domain.actions.literaturelists.sections.DeleteLiteratureListSectionAction.State
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionExistenceDeleteValidator(
    private val abstractLiteratureListExistenceValidator: AbstractLiteratureListExistenceValidator,
) : DeleteLiteratureListSectionAction {
    constructor(
        literatureListService: LiteratureListService,
        resourceRepository: ResourceRepository,
    ) : this(
        AbstractLiteratureListExistenceValidator(literatureListService, resourceRepository)
    )

    override fun invoke(command: DeleteLiteratureListSectionCommand, state: State): State =
        abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(command.literatureListId)
            .let { state.copy(literatureList = it.first, statements = it.second) }
}
