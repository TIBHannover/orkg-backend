package org.orkg.contenttypes.domain.actions.contribution

import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.TempIdValidator

class ContributionTempIdValidator : TempIdValidator(), ContributionAction {
    override fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val ids = command.tempIds()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
