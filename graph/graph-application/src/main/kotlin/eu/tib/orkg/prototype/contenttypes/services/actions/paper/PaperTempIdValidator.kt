package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.contenttypes.services.actions.TempIdValidator

class PaperTempIdValidator : TempIdValidator(), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val ids = command.contents?.tempIds().orEmpty()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
