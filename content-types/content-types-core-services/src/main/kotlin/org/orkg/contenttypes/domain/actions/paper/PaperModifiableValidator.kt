package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState

class PaperModifiableValidator : UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState =
        state.also { if (!state.paper!!.modifiable) throw PaperNotModifiable(command.paperId) }
}
