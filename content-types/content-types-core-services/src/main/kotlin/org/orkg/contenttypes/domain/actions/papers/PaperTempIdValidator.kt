package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.TempIdValidator

class PaperTempIdValidator : TempIdValidator(), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val ids = command.contents?.tempIds().orEmpty()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
