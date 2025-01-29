package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneTemplateCommand
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.testing.pageOf

internal class RosettaStoneTemplateModifiableValidatorUnitTest : MockkBaseTest {
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository = mockk()

    private val rosettaStoneTemplateModifiableValidator =
        RosettaStoneTemplateModifiableValidator(rosettaStoneStatementRepository)

    @Test
    fun `Given a rosetta stone template update command, when rosetta stone template is modifiable and template is used in a rosetta stone statement, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every {
            rosettaStoneStatementRepository.findAll(
                templateId = command.templateId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createRosettaStoneStatement()
        )

        rosettaStoneTemplateModifiableValidator(command, state).asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe state.statements
            it.isUsedInRosettaStoneStatement shouldBe true
        }

        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAll(
                templateId = command.templateId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when rosetta stone template is modifiable and template is not used in a rosetta stone statement, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every {
            rosettaStoneStatementRepository.findAll(
                templateId = command.templateId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        rosettaStoneTemplateModifiableValidator(command, state).asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe state.statements
            it.isUsedInRosettaStoneStatement shouldBe false
        }

        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAll(
                templateId = command.templateId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when rosetta stone template is not modifiable, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(modifiable = false)
        val command = updateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        shouldThrow<RosettaStoneTemplateNotModifiable> { rosettaStoneTemplateModifiableValidator(command, state) }
    }
}
