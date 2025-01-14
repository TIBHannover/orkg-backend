package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

internal class TemplateTargetClassCreatorUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()

    private val templateTargetClassCreator = TemplateTargetClassCreator(statementService)

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
