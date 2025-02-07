package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.stream.Stream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionCreator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.createLiteratureListListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.createLiteratureListTextSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class LiteratureListSectionCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator = mockk()
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val literatureListSectionCreator = LiteratureListSectionCreator(
        unsafeStatementUseCases, abstractLiteratureListSectionCreator, statementCollectionPropertyUpdater
    )

    @ParameterizedTest
    @MethodSource("createLiteratureListSectionCommands")
    fun `Given a literature list section create command, when no index is specified, it creates a new section and appends it to the existing literature list`(command: CreateLiteratureListSectionCommand) {
        val sectionId = ThingId("R456")
        val state = CreateLiteratureListSectionState()

        every {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        } returns sectionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.literatureListId,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId
                )
            )
        } returns StatementId("S1")

        val result = literatureListSectionCreator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.literatureListId,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId
                )
            )
        }
    }

    @Test
    fun `Given a literature list section create command, when index is specified, it creates a new section and links it to the existing literature list at the specified index`() {
        val sectionId = ThingId("R456")
        val command = createLiteratureListListSectionCommand().copy(index = 1)
        val statements = listOf(
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section1"))
            ),
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section2"))
            )
        )
        val state = CreateLiteratureListSectionState().copy(
            statements = statements.groupBy { it.subject.id }
        )

        every {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        } returns sectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs

        val result = literatureListSectionCreator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = listOf(ThingId("Section1"), sectionId, ThingId("Section2"))
            )
        }
    }

    @Test
    fun `Given a literature list section create command, when index is specified but higher than existing sections count, it creates a new section and appends it to the existing literature list`() {
        val sectionId = ThingId("R456")
        val command = createLiteratureListListSectionCommand().copy(index = 15)
        val statements = listOf(
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section1"))
            ),
            createStatement(
                subject = createResource(command.literatureListId),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(ThingId("Section2"))
            )
        )
        val state = CreateLiteratureListSectionState().copy(
            statements = statements.groupBy { it.subject.id }
        )

        every {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        } returns sectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs

        val result = literatureListSectionCreator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe sectionId
        }

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(
                contributorId = command.contributorId,
                section = command as LiteratureListSectionDefinition
            )
        }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = listOf(ThingId("Section1"), ThingId("Section2"), sectionId)
            )
        }
    }

    companion object {
        @JvmStatic
        fun createLiteratureListSectionCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(createLiteratureListListSectionCommand()),
            Arguments.of(createLiteratureListTextSectionCommand())
        )
    }
}
