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
import org.orkg.contenttypes.input.testing.fixtures.createTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases

internal class TemplateTargetClassCreatorUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()

    private val templateTargetClassCreator = TemplateTargetClassCreator(statementService)

    @Test
    fun `Given a template create command, it creates a new statement for the target class`() {
        val command = createTemplateCommand()
        val state = CreateTemplateState(
            templateId = ThingId("R45665")
        )

        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.shTargetClass,
                    objectId = command.targetClass
                )
            )
        } returns StatementId("S1")

        val result = templateTargetClassCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.shTargetClass,
                    objectId = command.targetClass
                )
            )
        }
    }
}
