package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.contenttypes.domain.actions.TempIdValidator

class PaperTempIdValidator : TempIdValidator(), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val ids = command.contents?.tempIds().orEmpty()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
