package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionState
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionExistenceDeleteValidator(
    private val literatureListService: LiteratureListService,
    private val resourceRepository: ResourceRepository
) : DeleteLiteratureListSectionAction {
    override fun invoke(
        command: DeleteLiteratureListSectionCommand,
        state: DeleteLiteratureListSectionState
    ): DeleteLiteratureListSectionState {
        val resource = resourceRepository.findById(command.literatureListId)
            .filter {
                if (Classes.literatureListPublished in it.classes) {
                    throw LiteratureListNotModifiable(command.literatureListId)
                } else {
                    Classes.literatureList in it.classes
                }
            }
            .orElseThrow { LiteratureListNotFound(command.literatureListId) }
        val subgraph = literatureListService.findSubgraph(resource)
        val literatureList = LiteratureList.from(resource, subgraph.root, subgraph.statements)
        return state.copy(literatureList = literatureList, statements = subgraph.statements)
    }
}
