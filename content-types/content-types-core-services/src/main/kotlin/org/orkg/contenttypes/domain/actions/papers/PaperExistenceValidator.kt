package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class PaperExistenceValidator(
    private val paperService: PaperService,
    private val resourceRepository: ResourceRepository,
) : UpdatePaperAction {
    override fun invoke(command: UpdatePaperCommand, state: State): State {
        val resource = resourceRepository.findById(command.paperId)
            .filter { Classes.paper in it.classes }
            .orElseThrow { PaperNotFound.withId(command.paperId) }
        val subgraph = paperService.findSubgraph(resource)
        val paper = Paper.from(resource, subgraph.statements)
        return state.copy(paper = paper, statements = subgraph.statements)
    }
}
