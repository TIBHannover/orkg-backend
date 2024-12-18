package org.orkg.contenttypes.domain.actions.comparisons

import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonPublicationInfoCreator(
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val clock: Clock = Clock.systemDefaultZone()
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        val comparisonId = state.comparisonId!!
        val now = OffsetDateTime.now(clock)
        val publicationYearLiteralId = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = now.year.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = comparisonId,
            predicate = Predicates.yearPublished,
            `object` = publicationYearLiteralId
        )
        val publicationMonthLiteralId = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = now.monthValue.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = comparisonId,
            predicate = Predicates.yearPublished,
            `object` = publicationMonthLiteralId
        )
        return state
    }
}
