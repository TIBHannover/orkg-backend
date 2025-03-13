package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.TemplateRelationsCommand
import org.orkg.contenttypes.input.testing.fixtures.createTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases

internal class TemplateRelationsCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val templateRelationsCreator = TemplateRelationsCreator(unsafeStatementUseCases)

    @Test
    fun `Given a template create command, when a related research field is set, it creates a new statement`() {
        val researchFieldId = ThingId("R41536")
        val command = createTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = listOf(researchFieldId),
                researchProblems = emptyList(),
                predicate = null
            )
        )
        val state = CreateTemplateState(
            templateId = ThingId("R45665")
        )

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateOfResearchField,
                    objectId = researchFieldId
                )
            )
        } returns StatementId("S1")

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateOfResearchField,
                    objectId = researchFieldId
                )
            )
        }
    }

    @Test
    fun `Given a template create command, when a related research problem is set, it creates a new statement`() {
        val researchProblemId = ThingId("R1456")
        val command = createTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = emptyList(),
                researchProblems = listOf(researchProblemId),
                predicate = null
            )
        )
        val state = CreateTemplateState(
            templateId = ThingId("R45665")
        )

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateOfResearchProblem,
                    objectId = researchProblemId
                )
            )
        } returns StatementId("S1")

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateOfResearchProblem,
                    objectId = researchProblemId
                )
            )
        }
    }

    @Test
    fun `Given a template create command, when a related predicate is set, it creates a new statement`() {
        val predicateId = ThingId("R145236")
        val command = createTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = emptyList(),
                researchProblems = emptyList(),
                predicate = predicateId
            )
        )
        val state = CreateTemplateState(
            templateId = ThingId("R45665")
        )

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateOfPredicate,
                    objectId = predicateId
                )
            )
        } returns StatementId("S1")

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateOfPredicate,
                    objectId = predicateId
                )
            )
        }
    }

    @Test
    fun `Given a template create command, when relations are empty, then no new statements are created`() {
        val command = createTemplateCommand().copy(
            relations = TemplateRelationsCommand(
                researchFields = emptyList(),
                researchProblems = emptyList(),
                predicate = null
            )
        )
        val state = CreateTemplateState(
            templateId = ThingId("R45665")
        )

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }
    }
}
