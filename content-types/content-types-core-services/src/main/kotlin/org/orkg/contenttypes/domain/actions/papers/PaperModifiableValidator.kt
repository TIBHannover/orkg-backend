package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State

class PaperModifiableValidator : UpdatePaperAction {
    override fun invoke(command: UpdatePaperCommand, state: State): State {
        if (!state.paper!!.modifiable) {
            throw PaperNotModifiable(command.paperId)
        }
        return state
    }
}
