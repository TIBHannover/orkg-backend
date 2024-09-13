package org.orkg.contenttypes.domain.actions.comparisons

import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonPublicationInfoUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
    private val clock: Clock = Clock.systemDefaultZone()
) : PublishComparisonAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
        clock: Clock
    ) : this(SingleStatementPropertyUpdater(literalService, statementService), clock)

    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val comparisonId = state.comparison!!.id
        val now = OffsetDateTime.now(clock)
        singleStatementPropertyUpdater.updateRequiredProperty(
            contributorId = command.contributorId,
            subjectId = comparisonId,
            predicateId = Predicates.yearPublished,
            label = now.year.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        singleStatementPropertyUpdater.updateRequiredProperty(
            contributorId = command.contributorId,
            subjectId = comparisonId,
            predicateId = Predicates.monthPublished,
            label = now.monthValue.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        return state
    }
}
