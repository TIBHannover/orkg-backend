package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperResearchFieldUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdatePaperAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService, unsafeStatementUseCases))

    override fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.researchFields != null && command.researchFields != state.paper!!.researchFields.ids) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.paperId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.paperId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!
            )
        }
        return state
    }
}
