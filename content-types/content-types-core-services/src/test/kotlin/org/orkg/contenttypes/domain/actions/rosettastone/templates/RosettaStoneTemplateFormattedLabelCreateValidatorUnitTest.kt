package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneTemplateCommand
import org.orkg.graph.domain.FormattedLabel

internal class RosettaStoneTemplateFormattedLabelCreateValidatorUnitTest {
    private val rosettaStoneTemplateFormattedLabelCreateValidator = RosettaStoneTemplateFormattedLabelCreateValidator()

    @Test
    fun `Given a create rosetta stone template command, when validating the formatted label, it returns success`() {
        val command = createRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState()

        val result = rosettaStoneTemplateFormattedLabelCreateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }
    }

    @Test
    fun `Given a create rosetta stone template command, when formatted label placeholder is missing, it throws an exception`() {
        val command = createRosettaStoneTemplateCommand().copy(formattedLabel = FormattedLabel.of("placeholder missing"))
        val state = CreateRosettaStoneTemplateState()

        assertThrows<MissingFormattedLabelPlaceholder> { rosettaStoneTemplateFormattedLabelCreateValidator(command, state) }
    }
}
