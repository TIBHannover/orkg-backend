package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.UnorderedCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonReferencesCreator(
    private val unorderedCollectionPropertyCreator: UnorderedCollectionPropertyCreator
) : ComparisonAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(UnorderedCollectionPropertyCreator(literalService, statementService))

    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.also {
            unorderedCollectionPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.reference,
                labels = command.references
            )
        }
}
