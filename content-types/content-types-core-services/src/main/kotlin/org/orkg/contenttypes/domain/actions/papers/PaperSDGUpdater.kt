package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.SDGUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.StatementUseCases

class PaperSDGUpdater(
    private val sdgUpdater: SDGUpdater
) : UpdatePaperAction {
    constructor(statementService: StatementUseCases) : this(SDGUpdater(statementService))

    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.sustainableDevelopmentGoals != null && command.sustainableDevelopmentGoals != state.paper!!.sustainableDevelopmentGoals.map { it.id }.toSet()) {
            sdgUpdater.update(command.contributorId, command.sustainableDevelopmentGoals!!, command.paperId)
        }
        return state
    }
}
