package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class LiteratureListSDGUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) : UpdateLiteratureListAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService))

    override operator fun invoke(
        command: UpdateLiteratureListCommand,
        state: UpdateLiteratureListState
    ): UpdateLiteratureListState {
        if (command.sustainableDevelopmentGoals != null && command.sustainableDevelopmentGoals != state.literatureList!!.sustainableDevelopmentGoals.ids) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.literatureListId].orEmpty()
                    .filter { it.predicate.id == Predicates.sustainableDevelopmentGoal },
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!,
            )
        }
        return state
    }
}
