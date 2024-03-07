package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.SDGCreator
import org.orkg.graph.input.StatementUseCases

class PaperSDGCreator(
    private val sdgCreator: SDGCreator
) : CreatePaperAction {
    constructor(statementService: StatementUseCases) : this(SDGCreator(statementService))

    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.also { sdgCreator.create(command.contributorId, command.sustainableDevelopmentGoals, state.paperId!!) }
}
