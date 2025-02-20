package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionState
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.stream.Stream

internal class SmartReviewSectionExistenceCreateValidatorUnitTest : MockkBaseTest {
    private val statementRepository: StatementRepository = mockk()

    private val smartReviewSectionExistenceCreateValidator =
        SmartReviewSectionExistenceCreateValidator(statementRepository)

    @ParameterizedTest
    @MethodSource("createSmartReviewSectionCommands")
    fun `Given a smart review section create command, when searching for an existing smart review, it returns success`(command: CreateSmartReviewSectionCommand) {
        val state = CreateSmartReviewSectionState()
        val contributionId = ThingId("R123")
        val statement = createStatement(
            subject = createResource(command.smartReviewId, classes = setOf(Classes.smartReview)),
            predicate = createPredicate(Predicates.hasContribution),
            `object` = createResource(contributionId, classes = setOf(Classes.contribution, Classes.contributionSmartReview))
        )

        every {
            statementRepository.findAll(
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasContribution,
                objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(statement)

        val result = smartReviewSectionExistenceCreateValidator(command, state)

        result.asClue {
            it.smartReviewSectionId shouldBe null
            it.contributionId shouldBe contributionId
            it.statements shouldBe emptyMap()
        }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasContribution,
                objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
                pageable = PageRequests.SINGLE
            )
        }
    }

    @ParameterizedTest
    @MethodSource("createSmartReviewSectionCommands")
    fun `Given a smart review section create command, when existing smart review is published, it throws an exception`(command: CreateSmartReviewSectionCommand) {
        val state = CreateSmartReviewSectionState()
        val contributionId = ThingId("R123")
        val statement = createStatement(
            subject = createResource(command.smartReviewId, classes = setOf(Classes.smartReviewPublished)),
            predicate = createPredicate(Predicates.hasContribution),
            `object` = createResource(contributionId, classes = setOf(Classes.contribution, Classes.contributionSmartReview))
        )

        every {
            statementRepository.findAll(
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasContribution,
                objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(statement)

        assertThrows<SmartReviewNotModifiable> { smartReviewSectionExistenceCreateValidator(command, state) }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasContribution,
                objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
                pageable = PageRequests.SINGLE
            )
        }
    }

    @ParameterizedTest
    @MethodSource("createSmartReviewSectionCommands")
    fun `Given a smart review section create command, when smart review does not exist, it throws an exception`(command: CreateSmartReviewSectionCommand) {
        val state = CreateSmartReviewSectionState()

        every {
            statementRepository.findAll(
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasContribution,
                objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertThrows<SmartReviewNotFound> { smartReviewSectionExistenceCreateValidator(command, state) }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasContribution,
                objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
                pageable = PageRequests.SINGLE
            )
        }
    }

    companion object {
        @JvmStatic
        fun createSmartReviewSectionCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(createSmartReviewComparisonSectionCommand()),
            Arguments.of(createSmartReviewVisualizationSectionCommand()),
            Arguments.of(createSmartReviewResourceSectionCommand()),
            Arguments.of(createSmartReviewPredicateSectionCommand()),
            Arguments.of(createSmartReviewOntologySectionCommand()),
            Arguments.of(createSmartReviewTextSectionCommand())
        )
    }
}
