package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.DeletePaperCommand
import org.orkg.contenttypes.domain.actions.papers.DeletePaperAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class PaperExistenceDeleteValidator(
    private val paperService: PaperService,
    private val resourceRepository: ResourceRepository,
) : DeletePaperAction {
    override fun invoke(command: DeletePaperCommand, state: State): State {
        val resource = resourceRepository.findById(command.paperId)
            .filter { Classes.paper in it.classes }
            .orElse(null)
        val subgraph = resource?.let(paperService::findSubgraph)
        return state.copy(paper = resource, statements = subgraph?.statements.orEmpty())
    }
}
