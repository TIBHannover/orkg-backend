package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class LiteratureListExistenceValidator(
    private val literatureListService: LiteratureListService,
    private val resourceRepository: ResourceRepository
) : UpdateLiteratureListAction {
    override fun invoke(command: UpdateLiteratureListCommand, state: UpdateLiteratureListState): UpdateLiteratureListState {
        val resource = resourceRepository.findById(command.literatureListId)
            .filter { Classes.literatureList in it.classes || Classes.literatureListPublished in it.classes }
            .orElseThrow { LiteratureListNotFound(command.literatureListId) }
        val subgraph = literatureListService.findSubgraph(resource)
        val literatureList = LiteratureList.from(resource, subgraph.root, subgraph.statements)
        return state.copy(literatureList = literatureList, statements = subgraph.statements)
    }
}
