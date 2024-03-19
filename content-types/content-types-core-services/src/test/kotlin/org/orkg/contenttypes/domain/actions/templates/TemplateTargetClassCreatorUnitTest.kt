package org.orkg.contenttypes.domain.actions.templates

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
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateTargetClassCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val templateTargetClassCreator = TemplateTargetClassCreator(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a template create command, it creates a new statement for the target class`() {
        val command = dummyCreateTemplateCommand()
        val state = CreateTemplateState(
            templateId = ThingId("R45665")
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shTargetClass,
                `object` = command.targetClass
            )
        } just runs

        val result = templateTargetClassCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shTargetClass,
                `object` = command.targetClass
            )
        }
    }
}
