package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionCreator
import org.orkg.contenttypes.input.AbstractSmartReviewSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.stream.Stream

internal class SmartReviewSectionCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator = mockk()
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val smartReviewSectionCreator = SmartReviewSectionCreator(
        unsafeStatementUseCases,
        abstractSmartReviewSectionCreator,
        statementCollectionPropertyUpdater
    )

    @ParameterizedTest
    @MethodSource("createSmartReviewSectionCommands")
    fun `Given a smart review section create command, when no index is specified, it creates a new section and appends it to the existing smart review contribution`(command: CreateSmartReviewSectionCommand) {
        val sectionId = ThingId("R456")
        val contributionId = ThingId("R789")
        val state = CreateSmartReviewSectionState(contributionId = contributionId)

        every {
            abstractSmartReviewSectionCreator.create(
                contributorId = command.contributorId,
                section = command as AbstractSmartReviewSectionCommand
            )
        } returns sectionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = contributionId,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId
                )
            )
        } returns StatementId("S1")

        val result = smartReviewSectionCreator(command, state)

        result.asClue {
            it.smartReviewSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractSmartReviewSectionCreator.create(
                contributorId = command.contributorId,
                section = command as AbstractSmartReviewSectionCommand
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = contributionId,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId
                )
            )
        }
    }

    @Test
    fun `Given a smart review section create command, when index is specified, it creates a new section and links it to the existing smart review contribution at the specified index`() {
        val sectionId = ThingId("R456")
        val contributionId = ThingId("R789")
        val command = createSmartReviewVisualizationSectionCommand().copy(index = 1)
        val statements = listOf(
            createStatement(
                subject = createResource(contributionId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section1"))
            ),
            createStatement(
                subject = createResource(contributionId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section2"))
            )
        )
        val state = CreateSmartReviewSectionState().copy(
            contributionId = contributionId,
            statements = statements.groupBy { it.subject.id }
        )

        every {
            abstractSmartReviewSectionCreator.create(
                contributorId = command.contributorId,
                section = command as AbstractSmartReviewSectionCommand
            )
        } returns sectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs

        val result = smartReviewSectionCreator(command, state)

        result.asClue {
            it.smartReviewSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractSmartReviewSectionCreator.create(
                contributorId = command.contributorId,
                section = command as AbstractSmartReviewSectionCommand
            )
        }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = listOf(ThingId("Section1"), sectionId, ThingId("Section2"))
            )
        }
    }

    @Test
    fun `Given a smart review section create command, when index is specified but higher than existing sections count, it creates a new section and appends it to the existing smart review contribution`() {
        val sectionId = ThingId("R456")
        val contributionId = ThingId("R789")
        val command = createSmartReviewVisualizationSectionCommand().copy(index = 15)
        val statements = listOf(
            createStatement(
                subject = createResource(contributionId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section1"))
            ),
            createStatement(
                subject = createResource(contributionId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section2"))
            )
        )
        val state = CreateSmartReviewSectionState().copy(
            contributionId = contributionId,
            statements = statements.groupBy { it.subject.id }
        )

        every {
            abstractSmartReviewSectionCreator.create(
                contributorId = command.contributorId,
                section = command as AbstractSmartReviewSectionCommand
            )
        } returns sectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs

        val result = smartReviewSectionCreator(command, state)

        result.asClue {
            it.smartReviewSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractSmartReviewSectionCreator.create(
                contributorId = command.contributorId,
                section = command as AbstractSmartReviewSectionCommand
            )
        }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = listOf(ThingId("Section1"), ThingId("Section2"), sectionId)
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
