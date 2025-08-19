package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.PublishPaperCommand
import org.orkg.contenttypes.domain.actions.papers.PublishPaperAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class PaperPublishableValidator(
    private val paperService: PaperService,
    private val resourceRepository: ResourceRepository,
) : PublishPaperAction {
    override fun invoke(command: PublishPaperCommand, state: State): State {
        val resource = resourceRepository.findById(command.id)
            .filter { Classes.paper in it.classes }
            .orElseThrow { PaperNotFound.withId(command.id) }
        val subgraph = paperService.findSubgraph(resource)
        val paper = Paper.from(resource, subgraph.statements)
        return state.copy(paper = paper, statements = subgraph.statements)
    }
}
