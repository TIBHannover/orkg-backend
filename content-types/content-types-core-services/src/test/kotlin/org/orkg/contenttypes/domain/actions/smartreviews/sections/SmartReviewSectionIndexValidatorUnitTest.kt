package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateSmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

internal class SmartReviewSectionIndexValidatorUnitTest : MockkBaseTest {
    private val statementRepository: StatementRepository = mockk()

    private val smartReviewSectionIndexValidator =
        SmartReviewSectionIndexValidator(statementRepository)

    @Test
    fun `Given a smart review section create command, when index is not specified, it does not load any statements`() {
        val command = dummyCreateSmartReviewVisualizationSectionCommand()
        val state = CreateSmartReviewSectionState(contributionId = ThingId("R54563"))

        val result = smartReviewSectionIndexValidator(command, state)

        result.asClue {
            it.smartReviewSectionId shouldBe null
            it.contributionId shouldBe state.contributionId
            it.statements shouldBe emptyMap()
        }
    }

    @Test
    fun `Given a smart review section create command, when index is specified and valid, it fetches and saves all hasSection statements to the state`() {
        val command = dummyCreateSmartReviewVisualizationSectionCommand().copy(index = 7)
        val state = CreateSmartReviewSectionState(contributionId = ThingId("R54563"))
        val statements = listOf(createStatement())

        every {
            statementRepository.findAll(
                subjectId = state.contributionId,
                predicateId = Predicates.hasSection,
                pageable = PageRequests.ALL
            )
        } returns pageOf(statements)

        val result = smartReviewSectionIndexValidator(command, state)

        result.asClue {
            it.smartReviewSectionId shouldBe null
            it.contributionId shouldBe state.contributionId
            it.statements shouldBe statements.groupBy { statement -> statement.subject.id }
        }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = state.contributionId,
                predicateId = Predicates.hasSection,
                pageable = PageRequests.ALL
            )
        }
    }
}
