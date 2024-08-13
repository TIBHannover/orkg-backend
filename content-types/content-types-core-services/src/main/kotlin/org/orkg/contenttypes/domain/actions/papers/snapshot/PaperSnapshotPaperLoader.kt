package org.orkg.contenttypes.domain.actions.papers.snapshot

import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.papers.snapshot.SnapshotPaperAction.State
import org.orkg.contenttypes.input.PublishPaperUseCase

class PaperSnapshotPaperLoader(
    private val paperService: PaperService
) : SnapshotPaperAction {
    override fun invoke(command: PublishPaperUseCase.PublishCommand, state: State): State {
        val subgraph = paperService.findSubgraph(state.resource!!)
        val paper = Paper.from(state.resource, subgraph.statements)
        return state.copy(paper = paper, statements = subgraph.statements)
    }
}
