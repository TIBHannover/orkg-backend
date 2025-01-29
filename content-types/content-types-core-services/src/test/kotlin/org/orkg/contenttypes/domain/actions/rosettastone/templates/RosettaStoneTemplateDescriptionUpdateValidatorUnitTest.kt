package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneTemplateCommand
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.MAX_LABEL_LENGTH

internal class RosettaStoneTemplateDescriptionUpdateValidatorUnitTest {
    private val rosettaStoneTemplateDescriptionUpdateValidator = RosettaStoneTemplateDescriptionUpdateValidator()

    @Test
    fun `Given a rosetta stone template update command, when description is valid, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        assertDoesNotThrow { rosettaStoneTemplateDescriptionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when description is null, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(description = null)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        assertDoesNotThrow { rosettaStoneTemplateDescriptionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when description is invalid, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(description = "a".repeat(MAX_LABEL_LENGTH + 1))
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate)

        assertThrows<InvalidDescription> { rosettaStoneTemplateDescriptionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when description is valid and template is used in a rosetta stone statement, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateInUse> { rosettaStoneTemplateDescriptionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when description is null and template is used in a rosetta stone statement, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(description = null)
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertDoesNotThrow { rosettaStoneTemplateDescriptionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template update command, when description is invalid and template is used in a rosetta stone statement, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(description = "a".repeat(MAX_LABEL_LENGTH + 1))
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateInUse> { rosettaStoneTemplateDescriptionUpdateValidator(command, state) }
    }
}
