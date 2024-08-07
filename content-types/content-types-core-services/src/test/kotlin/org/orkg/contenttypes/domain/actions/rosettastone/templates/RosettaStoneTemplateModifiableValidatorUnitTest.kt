package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneStatement
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.testing.pageOf

class RosettaStoneTemplateModifiableValidatorUnitTest {
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository = mockk()

    private val rosettaStoneTemplateModifiableValidator =
        RosettaStoneTemplateModifiableValidator(rosettaStoneStatementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(rosettaStoneStatementRepository)
    }

    @Test
    fun `Given a rosetta stone template update command, when rosetta stone template is modifiable and template is used in a rosetta stone statement, it returns success`() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every {
            rosettaStoneStatementRepository.findAll(
                templateId = command.templateId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createDummyRosettaStoneStatement()
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
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
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
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate().copy(modifiable = false)
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(templateId = rosettaStoneTemplate.id)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        shouldThrow<RosettaStoneTemplateNotModifiable> { rosettaStoneTemplateModifiableValidator(command, state) }
    }
}
