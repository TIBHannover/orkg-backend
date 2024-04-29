package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class LiteratureListResearchFieldUpdater(
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
        if (command.researchFields != null && command.researchFields != state.literatureList!!.researchFields.ids) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.literatureListId].orEmpty()
                    .filter { it.predicate.id == Predicates.hasResearchField },
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!.toSet()
            )
        }
        return state
    }
}
