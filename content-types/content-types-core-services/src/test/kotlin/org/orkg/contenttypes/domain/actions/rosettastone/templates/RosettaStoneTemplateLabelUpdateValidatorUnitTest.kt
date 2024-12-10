package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.graph.domain.InvalidLabel

internal class RosettaStoneTemplateLabelUpdateValidatorUnitTest {
    private val rosettaStoneTemplateLabelUpdateValidator = RosettaStoneTemplateLabelUpdateValidator()

    @Test
    fun `Given a rosetta stone template update command, when label is valid, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        assertDoesNotThrow { rosettaStoneTemplateLabelUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when label is null, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(label = null)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        assertDoesNotThrow { rosettaStoneTemplateLabelUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when label is invalid, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(label = "\n")
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        assertThrows<InvalidLabel> { rosettaStoneTemplateLabelUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when label is valid and template is used in a rosetta stone statement, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateInUse> { rosettaStoneTemplateLabelUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when label is null and template is used in a rosetta stone statement, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(label = null)
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertDoesNotThrow { rosettaStoneTemplateLabelUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when label is invalid and template is used in a rosetta stone statement, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(label = "\n")
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateInUse> { rosettaStoneTemplateLabelUpdateValidator(command, state) }
    }
}
