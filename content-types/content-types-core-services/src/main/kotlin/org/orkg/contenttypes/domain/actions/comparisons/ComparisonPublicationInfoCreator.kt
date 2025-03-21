package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.time.Clock
import java.time.OffsetDateTime

class ComparisonPublicationInfoCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val clock: Clock = Clock.systemDefaultZone(),
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        val comparisonId = state.comparisonId!!
        val now = OffsetDateTime.now(clock)
        val publicationYearLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = now.year.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = comparisonId,
                predicateId = Predicates.yearPublished,
                objectId = publicationYearLiteralId
            )
        )
        val publicationMonthLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = now.monthValue.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = comparisonId,
                predicateId = Predicates.yearPublished,
                objectId = publicationMonthLiteralId
            )
        )
        return state
    }
}
