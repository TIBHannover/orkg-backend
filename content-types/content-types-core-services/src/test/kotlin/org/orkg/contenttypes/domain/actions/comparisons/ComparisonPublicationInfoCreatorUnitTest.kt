package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

internal class ComparisonPublicationInfoCreatorUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val comparisonPublicationInfoCreator =
        ComparisonPublicationInfoCreator(statementService, literalService, fixedClock)

    @Test
    fun `Given a comparison publish command, it updates the publication metadata`() {
        val comparisonId = ThingId("R123")
        val command = createComparisonCommand()
        val state = CreateComparisonState(comparisonId = comparisonId)
        val now = OffsetDateTime.now(fixedClock)
        val publicationYearLiteralId = ThingId("L123")
        val publicationMonthLiteralId = ThingId("L456")

        every { literalService.create(any()) } returns publicationYearLiteralId andThen publicationMonthLiteralId
        every { statementService.add(any(), any(), any(), any()) } just runs

        comparisonPublicationInfoCreator(command, state).asClue {
            it.comparisonId shouldBe comparisonId
            it.authors shouldBe state.authors
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = now.year.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = comparisonId,
                predicate = Predicates.yearPublished,
                `object` = publicationYearLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = now.monthValue.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = comparisonId,
                predicate = Predicates.yearPublished,
                `object` = publicationMonthLiteralId
            )
        }
    }
}
