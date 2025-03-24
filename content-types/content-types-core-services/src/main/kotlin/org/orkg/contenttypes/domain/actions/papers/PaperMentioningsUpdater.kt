package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperMentioningsUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdatePaperAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdatePaperCommand, state: State): State {
        if (command.mentionings != null && command.mentionings != state.paper!!.mentionings.ids) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.paperId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.paperId,
                predicateId = Predicates.mentions,
                objects = command.mentionings!!
            )
        }
        return state
    }
}
