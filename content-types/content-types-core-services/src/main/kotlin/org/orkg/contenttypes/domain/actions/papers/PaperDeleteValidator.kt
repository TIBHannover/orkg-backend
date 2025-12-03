package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.PaperInUse
import org.orkg.contenttypes.domain.actions.DeletePaperCommand
import org.orkg.contenttypes.domain.actions.papers.DeletePaperAction.State
import org.orkg.graph.output.ThingRepository

class PaperDeleteValidator(
    private val thingRepository: ThingRepository,
) : DeletePaperAction {
    override fun invoke(command: DeletePaperCommand, state: State): State {
        if (state.paper != null) {
            if (thingRepository.isUsedAsObject(command.paperId)) {
                throw PaperInUse(command.paperId)
            }
        }
        return state
    }
}
