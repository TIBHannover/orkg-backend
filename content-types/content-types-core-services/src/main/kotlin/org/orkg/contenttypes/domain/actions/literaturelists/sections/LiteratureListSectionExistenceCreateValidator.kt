package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionExistenceCreateValidator(
    private val resourceRepository: ResourceRepository
) : CreateLiteratureListSectionAction {
    override fun invoke(
        command: CreateLiteratureListSectionCommand,
        state: CreateLiteratureListSectionState
    ): CreateLiteratureListSectionState {
        resourceRepository.findById(command.literatureListId)
            .filter {
                if (Classes.literatureListPublished in it.classes) {
                    throw LiteratureListNotModifiable(command.literatureListId)
                }
                Classes.literatureList in it.classes
            }
            .orElseThrow { LiteratureListNotFound(command.literatureListId) }
        return state
    }
}
