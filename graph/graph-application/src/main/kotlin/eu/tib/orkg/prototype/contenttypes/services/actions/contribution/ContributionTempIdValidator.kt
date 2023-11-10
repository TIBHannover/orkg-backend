package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.TempIdValidator

class ContributionTempIdValidator : TempIdValidator(), ContributionAction {
    override fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val ids = command.tempIds()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
