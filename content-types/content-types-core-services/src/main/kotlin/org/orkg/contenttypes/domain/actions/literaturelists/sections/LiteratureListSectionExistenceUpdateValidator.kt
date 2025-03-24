package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListExistenceValidator
import org.orkg.contenttypes.domain.actions.literaturelists.sections.UpdateLiteratureListSectionAction.State
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionExistenceUpdateValidator(
    private val abstractLiteratureListExistenceValidator: AbstractLiteratureListExistenceValidator,
) : UpdateLiteratureListSectionAction {
    constructor(
        literatureListService: LiteratureListService,
        resourceRepository: ResourceRepository,
    ) : this(
        AbstractLiteratureListExistenceValidator(literatureListService, resourceRepository)
    )

    override fun invoke(command: UpdateLiteratureListSectionCommand, state: State): State =
        abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(command.literatureListId)
            .let { state.copy(literatureList = it.first, statements = it.second) }
}
