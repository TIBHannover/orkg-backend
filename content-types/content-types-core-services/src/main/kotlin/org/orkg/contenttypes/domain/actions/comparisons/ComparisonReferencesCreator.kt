package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonReferencesCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator
) : CreateComparisonAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases
    ) : this(StatementCollectionPropertyCreator(literalService, unsafeStatementUseCases))

    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.also {
            statementCollectionPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.reference,
                labels = command.references
            )
        }
}
