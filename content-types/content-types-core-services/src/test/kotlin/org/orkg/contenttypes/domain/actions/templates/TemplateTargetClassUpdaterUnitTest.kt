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
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createStatement

internal class TemplateTargetClassUpdaterUnitTest : MockkBaseTest {
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val templateTargetClassUpdater = TemplateTargetClassUpdater(singleStatementPropertyUpdater)

    @Test
    fun `Given a template update command, when new target class has changed, it updates the target class statement`() {
        val command = updateTemplateCommand()
        val state = UpdateTemplateState(
            template = createTemplate(),
            statements = mapOf(command.templateId to listOf(createStatement()))
        )

        every { singleStatementPropertyUpdater.updateRequiredProperty(any(), any(), any(), any(), any<ThingId>()) } just runs

        val result = templateTargetClassUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = state.statements[command.templateId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.shTargetClass,
                objectId = command.targetClass!!
            )
        }
    }

    @Test
    fun `Given a template update command, when target class did not change, id does nothing`() {
        val command = updateTemplateCommand().copy(targetClass = ThingId("targetClass"))
        val state = UpdateTemplateState(template = createTemplate())

        val result = templateTargetClassUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }

    @Test
    fun `Given a template update command, when new target class is not set, id does nothing`() {
        val command = updateTemplateCommand().copy(targetClass = null)
        val state = UpdateTemplateState(template = createTemplate())

        val result = templateTargetClassUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }
}
