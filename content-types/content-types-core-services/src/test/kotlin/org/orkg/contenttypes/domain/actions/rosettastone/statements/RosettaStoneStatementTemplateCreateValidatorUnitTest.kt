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
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneStatementCommand

internal class RosettaStoneStatementTemplateCreateValidatorUnitTest : MockkBaseTest {
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases = mockk()

    private val rosettaStoneStatementTemplateCreateValidator = RosettaStoneStatementTemplateCreateValidator(rosettaStoneTemplateService)

    @Test
    fun `Given a rosetta stone statement create command, when validating the rosetta stone template, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = createRosettaStoneStatementCommand()
        val state = CreateRosettaStoneStatementState()

        every { rosettaStoneTemplateService.findById(command.templateId) } returns Optional.of(rosettaStoneTemplate)

        rosettaStoneStatementTemplateCreateValidator(command, state).asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
        }

        verify(exactly = 1) { rosettaStoneTemplateService.findById(command.templateId) }
    }

    @Test
    fun `Given a rosetta stone statement create command, when rosetta stone template is missing, it throws an exception`() {
        val command = createRosettaStoneStatementCommand()
        val state = CreateRosettaStoneStatementState()

        every { rosettaStoneTemplateService.findById(command.templateId) } returns Optional.empty()

        shouldThrow<RosettaStoneTemplateNotFound> { rosettaStoneStatementTemplateCreateValidator(command, state) }

        verify(exactly = 1) { rosettaStoneTemplateService.findById(command.templateId) }
    }
}
