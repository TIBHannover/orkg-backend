package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionDeleter
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class LiteratureListSectionDeleter(
    private val abstractLiteratureListSectionDeleter: AbstractLiteratureListSectionDeleter
) : DeleteLiteratureListSectionAction {
    constructor(
        statementService: StatementUseCases,
        resourceService: ResourceUseCases
    ) : this(AbstractLiteratureListSectionDeleter(statementService, resourceService))

    override fun invoke(
        command: DeleteLiteratureListSectionCommand,
        state: DeleteLiteratureListSectionState
    ): DeleteLiteratureListSectionState {
        val section = state.literatureList!!.sections.find { it.id == command.sectionId }
        if (section != null) {
            abstractLiteratureListSectionDeleter.delete(
                contributorId = command.contributorId,
                literatureListId = command.literatureListId,
                section = section,
                statements = state.statements
            )
        }
        return state
    }
}
