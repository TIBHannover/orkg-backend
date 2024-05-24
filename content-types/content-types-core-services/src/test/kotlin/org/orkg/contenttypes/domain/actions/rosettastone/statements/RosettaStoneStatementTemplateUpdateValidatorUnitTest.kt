package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneTemplate
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneStatementCommand

class RosettaStoneStatementTemplateUpdateValidatorUnitTest {
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases = mockk()

    private val rosettaStoneStatementTemplateUpdateValidator = RosettaStoneStatementTemplateUpdateValidator(rosettaStoneTemplateService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(rosettaStoneTemplateService)
    }

    @Test
    fun `Given a rosetta stone statement update command, when validating the rosetta stone template, it returns success`() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val rosettaStoneStatement = createDummyRosettaStoneStatement().copy(templateId = rosettaStoneTemplate.id)
        val command = dummyUpdateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState(rosettaStoneStatement = rosettaStoneStatement)

        every { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) } returns Optional.of(rosettaStoneTemplate)

        rosettaStoneStatementTemplateUpdateValidator(command, state).asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
        }

        verify(exactly = 1) { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) }
    }

    @Test
    fun `Given a rosetta stone statement update command, when rosetta stone template is missing, it throws an exception`() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val rosettaStoneStatement = createDummyRosettaStoneStatement().copy(templateId = rosettaStoneTemplate.id)
        val command = dummyUpdateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState(rosettaStoneStatement = rosettaStoneStatement)

        every { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) } returns Optional.empty()

        shouldThrow<RosettaStoneTemplateNotFound> { rosettaStoneStatementTemplateUpdateValidator(command, state) }

        verify(exactly = 1) { rosettaStoneTemplateService.findById(rosettaStoneStatement.templateId) }
    }
}
