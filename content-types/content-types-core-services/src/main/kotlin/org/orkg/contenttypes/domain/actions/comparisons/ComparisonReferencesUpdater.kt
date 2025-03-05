package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonReferencesUpdater(
    private val statementService: StatementUseCases,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        statementService,
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.references != null && command.references!! != state.comparison!!.references) {
            statementCollectionPropertyUpdater.update(
                statements = statementService.findAll(
                    subjectId = command.comparisonId,
                    predicateId = Predicates.reference,
                    objectClasses = setOf(Classes.literal),
                    pageable = PageRequests.ALL
                ).content,
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.reference,
                literals = command.references!!
            )
        }
        return state
    }
}
