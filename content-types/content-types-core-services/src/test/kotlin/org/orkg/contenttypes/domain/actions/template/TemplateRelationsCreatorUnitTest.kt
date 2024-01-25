package org.orkg.contenttypes.domain.actions.template

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateRelationsCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val templateRelationsCreator = TemplateRelationsCreator(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a template create command, when a related research field is set, it creates a new statement`() {
        val researchFieldId = ThingId("R41536")
        val command = dummyCreateTemplateCommand().copy(
            relations = CreateTemplateUseCase.CreateCommand.Relations(
                researchFields = listOf(researchFieldId),
                researchProblems = emptyList(),
                predicate = null
            )
        )
        val state = TemplateState(
            templateId = ThingId("R45665")
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfResearchField,
                `object` = researchFieldId
            )
        } just runs

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfResearchField,
                `object` = researchFieldId
            )
        }
    }

    @Test
    fun `Given a template create command, when a related research problem is set, it creates a new statement`() {
        val researchProblemId = ThingId("R1456")
        val command = dummyCreateTemplateCommand().copy(
            relations = CreateTemplateUseCase.CreateCommand.Relations(
                researchFields = emptyList(),
                researchProblems = listOf(researchProblemId),
                predicate = null
            )
        )
        val state = TemplateState(
            templateId = ThingId("R45665")
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfResearchProblem,
                `object` = researchProblemId
            )
        } just runs

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfResearchProblem,
                `object` = researchProblemId
            )
        }
    }

    @Test
    fun `Given a template create command, when a related predicate is set, it creates a new statement`() {
        val predicateId = ThingId("R145236")
        val command = dummyCreateTemplateCommand().copy(
            relations = CreateTemplateUseCase.CreateCommand.Relations(
                researchFields = emptyList(),
                researchProblems = emptyList(),
                predicate = predicateId
            )
        )
        val state = TemplateState(
            templateId = ThingId("R45665")
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfPredicate,
                `object` = predicateId
            )
        } just runs

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateOfPredicate,
                `object` = predicateId
            )
        }
    }

    @Test
    fun `Given a template create command, when relations are empty, then no new statements are created`() {
        val command = dummyCreateTemplateCommand().copy(
            relations = CreateTemplateUseCase.CreateCommand.Relations(
                researchFields = emptyList(),
                researchProblems = emptyList(),
                predicate = null
            )
        )
        val state = TemplateState(
            templateId = ThingId("R45665")
        )

        val result = templateRelationsCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }
    }
}
