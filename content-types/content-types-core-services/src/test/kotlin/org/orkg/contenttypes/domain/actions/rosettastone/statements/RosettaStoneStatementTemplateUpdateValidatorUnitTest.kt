package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneStatementCommand

internal class RosettaStoneStatementTemplateUpdateValidatorUnitTest : MockkBaseTest {
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases = mockk()

    private val rosettaStoneStatementTemplateUpdateValidator = RosettaStoneStatementTemplateUpdateValidator(rosettaStoneTemplateService)

    @Test
    fun `Given a rosetta stone statement update command, when validating the rosetta stone template, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val rosettaStoneStatement = createRosettaStoneStatement().copy(templateId = rosettaStoneTemplate.id)
        val command = updateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState(rosettaStoneStatement = rosettaStoneStatement)

        every { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) } returns Optional.of(rosettaStoneTemplate)

        rosettaStoneStatementTemplateUpdateValidator(command, state).asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
        }

        verify(exactly = 1) { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) }
    }

    @Test
    fun `Given a rosetta stone statement update command, when rosetta stone template is missing, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val rosettaStoneStatement = createRosettaStoneStatement().copy(templateId = rosettaStoneTemplate.id)
        val command = updateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState(rosettaStoneStatement = rosettaStoneStatement)

        every { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) } returns Optional.empty()

        shouldThrow<RosettaStoneTemplateNotFound> { rosettaStoneStatementTemplateUpdateValidator(command, state) }

        verify(exactly = 1) { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) }
    }
}
